(ns matvaretabellen.ui.table
  (:require [matvaretabellen.ui.dom :as dom]))

(defn init-checkboxes [filter-panel table]
  (->> (fn [e]
         (when-let [id (some-> (.-target e)
                               .-parentNode
                               (.getAttribute "data-filter-id"))]
           (let [cells (dom/qsa table (str "[data-id=\"" id "\"]"))]
             (if (.-checked (.-target e))
               (js/requestAnimationFrame #(doall (map dom/show cells)))
               (js/requestAnimationFrame #(doall (map dom/hide cells)))))))
       (.addEventListener filter-panel "input")))

(defn init-giant-table [filter-panel table]
  (init-checkboxes filter-panel table))
