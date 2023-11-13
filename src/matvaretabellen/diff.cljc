(ns matvaretabellen.diff
  (:require [broch.core :as b]))

(defn ->nutrient-lookup [constituents]
  (->> constituents
       (map (juxt (comp :nutrient/id :constituent/nutrient) :measurement/quantity))
       (into {})))

(defn get-nutrient-group-lookup [food]
  (->> (:food/constituents food)
       (remove (comp :nutrient/parent :constituent/nutrient))
       ->nutrient-lookup))

(defn food->diffable [food]
  [(:food/id food) (get-nutrient-group-lookup food)])

(defn diff-quantities [ref-vals m1 m2]
  (->> (for [[k v] m1]
         [k (let [r (get ref-vals k)]
              (- (if-let [m (get m2 k)]
                   (/ (b/num m) r)
                   0)
                 (if v
                   (/ (b/num v) r)
                   0)))])
       (into {})))

(defn diff-constituents [ref-vals reference & xs]
  (->> xs
       (map (fn [[id k->m]]
              {:id id
               :diffs (diff-quantities ref-vals (second reference) k->m)}))))

(defn rate-energy-diff [reference-food & foods]
  (let [ref (:measurement/quantity (:food/energy reference-food))]
    (for [food foods]
      {:food/id (:food/id food)
       :diff (b// ref (:measurement/quantity (:food/energy food)))
       :rating (let [diff (apply b// (sort [ref (:measurement/quantity (:food/energy food))]))]
                 diff)})))
