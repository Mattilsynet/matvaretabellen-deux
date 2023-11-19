(ns matvaretabellen.ingest
  (:require [clojure.java.io :as io]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.excel :as excel]
            [matvaretabellen.foodcase-import :as import]
            [matvaretabellen.mashdown :as mashdown]
            [matvaretabellen.pages :as pages]
            [matvaretabellen.rda :as rda]
            [matvaretabellen.urls :as urls]))

(defn with-open-graph [m {:keys [title description image]}]
  (cond-> m
    title (assoc :open-graph/title (str title " - Matvaretabellen.no"))
    description (assoc :open-graph/description (mashdown/strip description))
    image (assoc :open-graph/image image)))

(defn get-food-pages [db]
  (->> (d/q '[:find ?food-id ?food-name
              :where
              [?f :food/id ?food-id]
              [?f :food/name ?food-name]]
            db)
       (mapcat
        (fn [[id i18n-names]]
          (for [[locale food-name] i18n-names]
            (-> {:page/uri (urls/get-food-url locale food-name)
                 :page/kind :page.kind/food
                 :page/locale locale
                 :page/food-id id}
                (with-open-graph
                  {:title food-name})))))))

(defn get-nutrient-pages [food-db app-db]
  (->> (d/q '[:find ?nutrient-id ?nutrient-name
              :where
              [?n :nutrient/id ?nutrient-id]
              [?n :nutrient/name ?nutrient-name]]
            food-db)
       (mapcat
        (fn [[id i18n-names]]
          (mapcat
           (fn [[locale nutrient-name]]
             (let [nutrient (d/entity app-db [:nutrient/id id])]
               [(-> {:page/uri (urls/get-nutrient-url locale nutrient-name)
                     :page/kind :page.kind/nutrient
                     :page/locale locale
                     :page/nutrient-id id}
                    (with-open-graph
                      {:title nutrient-name
                       :description (get-in nutrient [:nutrient/short-description locale])
                       :image (:nutrient/photo nutrient)}))
                {:page/uri (urls/get-nutrient-excel-url locale nutrient-name)
                 :page/kind :page.kind/nutrient-excel
                 :page/locale locale
                 :page/nutrient-id id}]))
           i18n-names)))))

(defn get-food-group-pages [foods-db app-db]
  (->> (d/q '[:find ?id ?name
              :where
              [?f :food-group/id ?id]
              [?f :food-group/name ?name]]
            foods-db)
       (mapcat
        (fn [[id i18n-names]]
          (mapcat
           (fn [[locale name]]
             (let [food-group (d/entity app-db [:food-group/id id])]
               [(-> {:page/uri (urls/get-food-group-url locale name)
                     :page/kind :page.kind/food-group
                     :page/locale locale
                     :page/food-group-id id}
                    (with-open-graph
                      {:title name
                       :image (:food-group/photo food-group)
                       :description (get-in food-group [:food-group/short-description locale])}))
                {:page/uri (urls/get-food-group-excel-url locale name)
                 :page/kind :page.kind/food-group-excel
                 :page/locale locale
                 :page/food-group-id id}]))
           i18n-names)))))

(defn ensure-unique-page-uris [entity-maps]
  (when-not (= (count entity-maps)
               (count (set (map :page/uri entity-maps))))
    (throw (ex-info "Duplicate :page/uri detected, awooooga, awoooga!"
                    {:duplicates (->> (map :page/uri entity-maps)
                                      frequencies
                                      (remove (comp #{1} val)))})))
  entity-maps)

(defn add-excel-etags [pages]
  (let [etag (str excel/version "-" (import/get-last-modified))]
    (for [page pages]
      (cond-> page
        (#{:page.kind/nutrient-excel
           :page.kind/food-group-excel
           :page.kind/foods-excel} (:page/kind page))
        (assoc :page/etag etag)))))

(defn on-started [foods-conn powerpack-app]
  (let [db (d/db foods-conn)
        app-db (d/db (:datomic/conn powerpack-app))
        rda-profiles (rda/read-csv db (slurp (io/file "data/adi.csv")))]
    (->> (concat (pages/get-static-pages)
                 (get-food-pages db)
                 (get-food-group-pages db app-db)
                 (get-nutrient-pages db app-db))
         ;;add-excel-etags
         (ensure-unique-page-uris)
         (concat rda-profiles)
         (d/transact (:datomic/conn powerpack-app))
         deref)))

(defn create-tx [_file-name datas]
  datas)
