(ns matvaretabellen.ingest
  (:require [clojure.java.io :as io]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.pages :as pages]
            [matvaretabellen.rda :as rda]
            [matvaretabellen.urls :as urls]))

(defn get-food-pages [db]
  (->> (d/q '[:find ?food-id ?food-name
              :where
              [?f :food/id ?food-id]
              [?f :food/name ?food-name]]
            db)
       (mapcat
        (fn [[id i18n-names]]
          (for [[locale food-name] i18n-names]
            {:page/uri (urls/get-food-url locale food-name)
             :page/kind :page.kind/food
             :page/locale locale
             :page/food-id id})))))

(defn get-nutrient-pages [db]
  (->> (d/q '[:find ?nutrient-id ?nutrient-name
              :where
              [?n :nutrient/id ?nutrient-id]
              [?n :nutrient/name ?nutrient-name]]
            db)
       (mapcat
        (fn [[id i18n-names]]
          (mapcat
           (fn [[locale nutrient-name]]
             [{:page/uri (urls/get-nutrient-url locale nutrient-name)
               :page/kind :page.kind/nutrient
               :page/locale locale
               :page/nutrient-id id}
              {:page/uri (urls/get-nutrient-excel-url locale nutrient-name)
               :page/kind :page.kind/nutrient-excel
               :page/locale locale
               :page/nutrient-id id}])
           i18n-names)))))

(defn get-food-group-pages [db]
  (->> (d/q '[:find ?id ?name
              :where
              [?f :food-group/id ?id]
              [?f :food-group/name ?name]]
            db)
       (mapcat
        (fn [[id i18n-names]]
          (mapcat
           (fn [[locale name]]
             [{:page/uri (urls/get-food-group-url locale name)
               :page/kind :page.kind/food-group
               :page/locale locale
               :page/food-group-id id}
              {:page/uri (urls/get-food-group-excel-url locale name)
               :page/kind :page.kind/food-group-excel
               :page/locale locale
               :page/food-group-id id}])
           i18n-names)))))

(defn ensure-unique-page-uris [entity-maps]
  (when-not (= (count entity-maps)
               (count (set (map :page/uri entity-maps))))
    (throw (ex-info "Duplicate :page/uri detected, awooooga, awoooga!"
                    {:duplicates (->> (map :page/uri entity-maps)
                                      frequencies
                                      (remove (comp #{1} val)))})))
  entity-maps)

(defn on-started [foods-conn powerpack-app]
  (let [db (d/db foods-conn)
        rda-profiles (rda/read-csv db (slurp (io/file "data/adi.csv")))]
    (->> (concat (pages/get-static-pages)
                 (get-food-pages db)
                 (get-food-group-pages db)
                 (get-nutrient-pages db)
                 (rda/get-rda-pages [:nb :en] rda-profiles))
         (ensure-unique-page-uris)
         (concat rda-profiles)
         (d/transact (:datomic/conn powerpack-app))
         deref)))

(defn create-tx [_file-name datas]
  datas)
