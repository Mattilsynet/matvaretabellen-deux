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

(defn on-started [foods-conn powerpack-app]
  (->> pages/static-pages
       (concat (get-food-pages (d/db foods-conn)))
       (d/transact (:datomic/conn powerpack-app))
       deref))

(defn create-tx [_file-name datas]
  datas)
