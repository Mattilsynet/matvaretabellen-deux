(ns matvaretabellen.seeded-random)

(defn shuffle* [seed ^java.util.Collection coll]
  (let [al (java.util.ArrayList. coll)]
    (java.util.Collections/shuffle al (java.util.Random. seed))
    (clojure.lang.RT/vector (.toArray al))))
