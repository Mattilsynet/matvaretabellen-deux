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

(def severity [::similar ::slight ::moderate ::significant ::dramatic])

(defn get-rating-severity [rating]
  (.indexOf severity rating))

(defn rate-energy-diff [[[_ ref-energy] & xs]]
  (for [[id energy] xs]
    {:id id
     :diff (/ ref-energy energy)
     :rating (let [diff (apply / (reverse (sort [ref-energy energy])))]
               (cond
                 (< diff 1.1) ::similar
                 (< diff 1.25) ::slight
                 (< diff 1.5) ::moderate
                 (< diff 2) ::significant
                 :else ::dramatic))}))

(defn get-energy-equivalents [[[_ ref-energy] & xs]]
  (for [[id energy] xs]
    {:id id
     :amount (double (/ ref-energy energy))}))
