(ns matvaretabellen.ui.sortable-table
  (:require [matvaretabellen.ui.dom :as dom]))

(defn get-sort-params [^js th]
  {:attribute (.-sortBy (.-dataset th))
   :current-order (dom/get-attr th "aria-sort")
   :type (.-sortType (.-dataset th))})

(def next-order
  {"ascending" "descending"
   "descending" "ascending"})

(defn sort-rows [rows {:keys [attribute type]} order]
  (let [coerce (if (= "number" type)
                 parse-double
                 identity)]
    (->> (map-indexed
          (fn [idx row]
            [idx (or (some-> (dom/qs row (str "[" attribute "]"))
                             (.getAttribute (str attribute))
                             coerce)
                     0)])
          (seq rows))
         (sort-by second (if (= order "ascending") < >)))))

(defn sort-table [table th params]
  (let [target-order (next-order (:current-order params))
        rows (dom/qsa table "tbody tr")
        tbody (dom/qs table "tbody")]
    (dom/set-attr th "aria-sort" target-order)
    (doseq [[idx] (sort-rows rows params target-order)]
      (.appendChild tbody (nth rows idx)))))

(defn init [^js table]
  (doseq [th (dom/qsa table "thead [data-sort-by]")]
    (.addEventListener th "click" (fn [_] (sort-table table th (get-sort-params th))))))
