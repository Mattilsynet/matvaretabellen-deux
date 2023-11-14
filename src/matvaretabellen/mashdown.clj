(ns matvaretabellen.mashdown
  "A markdown and potato mashup."
  (:require [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.urls :as urls]))

(defn find-url-for-segment [db locale id]
  (let [nutrient (d/entity db [:nutrient/id id])
        food (d/entity db [:food/id id])
        food-group (when (str/starts-with? id "fg-")
                     (d/entity db [:food-group/id (subs id 3)]))]
    (cond
      nutrient (urls/get-nutrient-url locale nutrient)
      food (urls/get-food-url locale food)
      food-group (urls/get-food-group-url locale food-group)
      :else (throw (ex-info (str "Unknown mashdown segment id: " id)
                            {:locale locale :id id})))))

(def bracket-pattern
  (re-pattern "\\[.*?\\]|[^\\[]+"))

(defn strip [s]
  (when s
    (str/join
     (for [segment (re-seq bracket-pattern s)]
       (if (= \[ (first segment))
         (let [segment (str/replace segment #"\[|\]" "")
               [pre _] (str/split segment #"\|")]
           pre)
         segment)))))

(defn render [db locale s]
  (when s
    (for [segment (re-seq bracket-pattern s)]
      (if (= \[ (first segment))
        (let [segment (str/replace segment #"\[|\]" "")
              [pre post] (str/split segment #"\|")
              id (or post (str/capitalize pre))]
          [:a {:href (find-url-for-segment db locale id)}
           pre])
        segment))))
