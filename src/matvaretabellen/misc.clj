(ns matvaretabellen.misc)

(defn natural-order-comparator-ish [s]
  (let [initial-numbers (re-find #"^\d+" s)
        first-numbers (re-find #"\d+" s)
        first-letters (re-find #"\D+" s)]
    (if initial-numbers
      ["" (parse-long initial-numbers) s]
      [first-letters (parse-long (or first-numbers "0")) s])))
