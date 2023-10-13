(ns matvaretabellen.ingest
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.pages :as pages]
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
            {:page/uri (urls/get-url locale food-name)
             :page/kind :page.kind/food
             :page/locale locale
             :food/id id})))))

(defn ensure-unique-page-uris [entity-maps]
  (when-not (= (count entity-maps)
               (count (set (map :page/uri entity-maps))))
    (throw (ex-info "Duplicate :page/uri detected, awooooga, awoooga!"
                    {:duplicates (->> (map :page/uri entity-maps)
                                      frequencies
                                      (remove (comp #{1} val)))})))
  entity-maps)

(defn on-started [foods-conn powerpack-app]
  (->> (concat pages/static-pages
               (get-food-pages (d/db foods-conn)))
       (ensure-unique-page-uris)
       (d/transact (:datomic/conn powerpack-app))
       deref))

(defn create-tx [_file-name datas]
  datas)
