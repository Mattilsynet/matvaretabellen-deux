(ns matvaretabellen.seeded-random)

(defn shuffle* [seed ^java.util.Collection coll]
  (let [al (java.util.ArrayList. coll)]
    (java.util.Collections/shuffle al (java.util.Random. seed))
    (clojure.lang.RT/vector (.toArray al))))

(defn rand-int* [seed n]
  (mod (.nextInt (java.util.Random. seed) (* 23 n)) n))

(defn rand-nth* [seed coll]
  (nth coll (rand-int* seed (count coll))))
