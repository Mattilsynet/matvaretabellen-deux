(ns matvaretabellen.food
  (:require [matvaretabellen.nutrient :as nutrient]))

(defn get-nutrient-parts [nutrient-id food]
  (->> (:food/constituents food)
       (map :constituent/nutrient)
       (filter (comp #{nutrient-id}
                     :nutrient/id
                     :nutrient/parent))
       nutrient/sort-by-preference))
