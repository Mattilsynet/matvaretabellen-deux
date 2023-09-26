(ns matvaretabellen.ingest
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [broch.core :as b]))

(defn load-json [file-name]
  (-> (io/file file-name)
      slurp
      json/read-str))

(defn get-words [s & [re]]
  (str/split (str/trim s) (or re #" +")))

(def known-non-constituents
  ["Netto"
   "Energi1"
   "Energi2"
   "Portion"])

(defn get-constituents [food]
  (set
   (for [[id {:strs [ref value]}] (->> (apply dissoc food known-non-constituents)
                                       (filter (fn [[k v]] (get v "ref"))))]
     (let [grams (parse-double value)]
       (cond-> {:constituent/nutrient [:nutrient/id id]
                :measurement/source [:source/id ref]}
         grams (assoc :measurement/quantity (b/grams grams)))))))

(defn get-portions [{:strs [ref value]}]
  (set
   (map (fn [r v]
          {:portion/kind [:portion-kind/id (keyword r)]
           :portion/quantity (b/grams (parse-double v))})
        (get-words ref)
        (get-words value))))

(defn foodcase-food->food [{:strs [id name slug groupId synonym latinName Netto
                                   langualCodes Vann Energi1 Energi2 Portion] :as food}]
  (cond-> {:food/id id
           :food/name name
           :food/slug slug
           :food/food-group [:food-group/id groupId]
           :food/search-keywords (set (get-words synonym #";"))
           :food/latin-name latinName
           :food/langual-codes (set (for [id (get-words langualCodes)]
                                      [:langual-code/id id]))
           :food/energy {:measurement/quantity (b/joules (parse-double (get Energi1 "value")))
                         :measurement/source [:origin/id (get Energi1 "ref")]}
           :food/calories {:measurement/observation (get Energi2 "value")
                           :measurement/source [:origin/id (get Energi2 "ref")]}
           :food/constituents (get-constituents food)

           :food/portions (get-portions Portion)}
    (not-empty (get Netto "value"))
    (assoc :food/edible-part
           {:measurement/percent (Math/round (parse-double (get Netto "value")))
            :measurement/source [:origin/id (get Netto "ref")]})))

(defn prepare-foodcase-foods [file-name]
  (->> (load-json file-name)
       :foods
       (map foodcase-food->food)))
