(ns matvaretabellen.ui.table
  (:require [matvaretabellen.ui.dom :as dom]))

(defn init-giant-table [filter-panel table]
  (->> (fn [e]
         (when-let [id (some-> (.-target e)
                               .-parentNode
                               (.getAttribute "data-filter-id"))]
           (let [cells (dom/qsa table (str "[data-id=\"" id "\"]"))]
             (if (.-checked (.-target e))
               (doall (map dom/show cells))
               (doall (map dom/hide cells))))))
       (.addEventListener filter-panel "input"))
  (when-let [toggler (dom/qs "[data-toggle-target]")]
    (->> (fn [e]
           (when-let [target (some-> (.-target e)
                                     (.getAttribute "data-toggle-target")
                                     dom/qs)]
             (if (dom/has-class target "mmm-hidden")
               (dom/show target)
               (dom/hide target))))
         (.addEventListener toggler "click"))))
