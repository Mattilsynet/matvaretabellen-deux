(ns matvaretabellen.misc
  (:require [broch.core :as b])
  (:import (java.text NumberFormat)
           (java.util Locale)))

(b/defunit-once kilojoules :energy "kJ" 1000 {b/joules 1})
(b/defunit-once mg-ate :mass "mg-ATE" 1.0E-6)
(b/defunit-once ug-re :mass "µg-RE" 1.0E-9)

(def locales
  {:nb (Locale/forLanguageTag "nb-NO")
   :en (Locale/forLanguageTag "en-GB")})

(defn format-number [locale n & [{:keys [decimals]}]]
  (let [formatter (NumberFormat/getNumberInstance (locales locale))]
    (when decimals
      (.setMaximumFractionDigits formatter decimals))
    (.format formatter n)))

(defn natural-order-comparator-ish [s]
  (let [initial-numbers (re-find #"^\d+" s)
        first-numbers (re-find #"\d+" s)
        first-letters (re-find #"\D+" s)]
    (if initial-numbers
      ["" (parse-long initial-numbers) s]
      [first-letters (parse-long (or first-numbers "0")) s])))

(defn power-of-ten? [x]
  (let [n (Math/log10 x)]
    (= (Math/floor n) n)))

(def mass-units
  (->> (b/compatible-units (b/kilograms))
       (filter (comp power-of-ten? :broch/scaled b/composition))
       (sort-by (comp :broch/scaled b/composition))
       vec))

(def synonyms
  {"µg-RE" #broch/quantity[nil "µg"]
   "mg-ATE" #broch/quantity[nil "mg"]})

(def synonym-lookup
  (into {} (map (juxt (comp b/symbol val) key) synonyms)))

(defn get-conversion [idx]
  (when-let [unit (get mass-units idx)]
    (get synonyms (b/symbol unit) unit)))

(defn get-unit-idx [symbol]
  (->> (synonym-lookup symbol symbol)
       (.indexOf (map b/symbol mass-units))))

(defn convert-to-readable-unit [quantity]
  (let [idx (cond
              (< 1000 (b/num quantity))
              (inc (get-unit-idx quantity))

              (< (b/num quantity) 1)
              (dec (get-unit-idx quantity)))]
    (if-let [conversion (get mass-units idx)]
      (b/quantity conversion quantity)
      quantity)))

(defn ->map [x]
  (cond
    (:db/id x) (update-vals (into {:db/id (:db/id x)} x) ->map)
    (map? x) (update-vals x ->map)
    (coll? x) (map ->map x)
    :else x))

(defn update-existing [m k & args]
  (if (contains? m k)
    (apply update m k args)
    m))

(defn summarize-constituent [constituent]
  {:constituent/nutrient (-> (:constituent/nutrient constituent)
                             (select-keys [:nutrient/id :nutrient/parent])
                             (update-existing :nutrient/parent :nutrient/id))
   :measurement/quantity (:measurement/quantity constituent)})

(defn summarize-food [food]
  (-> food
      (select-keys [:food/id :food/calories :food/name :food/search-keywords :food/energy :food/constituents])
      (update :food/calories select-keys [:measurement/observation])
      (update :food/name select-keys [:nb])
      (update :food/energy select-keys [:measurement/quantity])
      (update :food/constituents #(mapv summarize-constituent %))))
