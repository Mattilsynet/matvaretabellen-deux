(ns matvaretabellen.ui.toggler
  (:require [matvaretabellen.ui.dom :as dom]))

(defn init-toggler [toggler]
  (->> (fn [e]
         (when-let [target (some-> (.-target e)
                                   (.getAttribute "data-toggle-target")
                                   dom/qs)]
           (if (dom/has-class target "mmm-hidden")
             (dom/show target)
             (dom/hide target))))
       (.addEventListener toggler "click")))

(defn init []
  (doall (map init-toggler (dom/qsa "[data-toggle-target]"))))
