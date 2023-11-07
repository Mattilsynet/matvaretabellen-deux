(ns matvaretabellen.ingest
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.pages :as pages]
            [matvaretabellen.urls :as urls]))

(defn load-edn [file-name]
  (-> (io/file file-name)
      slurp
      edn/read-string))

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

(defn get-food-group-pages [db]
  (->> (d/q '[:find ?id ?name
              :where
              [?f :food-group/id ?id]
              [?f :food-group/name ?name]]
            db)
       (mapcat
        (fn [[id i18n-names]]
          (for [[locale name] i18n-names]
            {:page/uri (urls/get-food-group-url locale name)
             :page/kind :page.kind/food-group
             :page/locale locale
             :page/food-group-id id})))))

(defn ensure-unique-page-uris [entity-maps]
  (when-not (= (count entity-maps)
               (count (set (map :page/uri entity-maps))))
    (throw (ex-info "Duplicate :page/uri detected, awooooga, awoooga!"
                    {:duplicates (->> (map :page/uri entity-maps)
                                      frequencies
                                      (remove (comp #{1} val)))})))
  entity-maps)

(defn on-started [foods-conn powerpack-app]
  (let [db (d/db foods-conn)]
    (->> (concat (pages/get-static-pages)
                 (get-food-pages db)
                 (get-food-group-pages db))
         (ensure-unique-page-uris)
         (concat (load-edn "data/food-group-embellishments.edn"))
         (d/transact (:datomic/conn powerpack-app))
         deref)))

(defn create-tx [_file-name datas]
  datas)
