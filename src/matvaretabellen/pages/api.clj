(ns matvaretabellen.pages.api
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.food :as food]))

(defn render-compact-foods [context page]
  {:content-type :json
   :body (->> (d/q '[:find [?f ...]
                     :where
                     [?f :food/id]]
                   (:foods/db context))
              (map #(d/entity (:foods/db context) %))
              (sort-by (comp (:page/locale page) :food/name))
              (map #(food/food->compact-api-data (:page/locale page) %)))})
