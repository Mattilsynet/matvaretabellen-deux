(ns matvaretabellen.statistics)

(defn get-mean [xs]
  (when (seq xs)
    (/ (reduce + 0 xs) (count xs))))

(defn get-median [xs]
  (when-not (empty? xs)
    (let [xs (sort xs)
          n (count xs)]
      (if (odd? n)
        (nth xs (quot n 2))
        (/ (+ (nth xs (quot n 2))
              (nth xs (dec (quot n 2))))
           2)))))
