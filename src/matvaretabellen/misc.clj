(ns matvaretabellen.misc
  (:require [broch.core :as b]))

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
