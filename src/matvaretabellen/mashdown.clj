(ns matvaretabellen.mashdown
  "A markdown and potato mashup."
  (:require [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.urls :as urls]))

(defn find-url-for-segment [db locale id]
  (let [nutrient (d/entity db [:nutrient/id (str/capitalize id)])
        food (d/entity db [:food/id id])]
    (cond
      nutrient (urls/get-nutrient-url locale nutrient)
      food (urls/get-food-url locale food)
      :else (throw (ex-info (str "Unknown mashdown segment id: " id)
                            {:locale locale :id id})))))

(def bracket-pattern
  (re-pattern "\\[.*?\\]|[^\\[]+"))

(defn render [db locale s]
  (for [segment (re-seq bracket-pattern s)]
    (if (= \[ (first segment))
      (let [segment (str/replace segment #"\[|\]" "")
            [pre post] (str/split segment #"\|")
            id (or post pre)]
        [:a {:href (find-url-for-segment db locale id)}
         pre])
      segment)))
