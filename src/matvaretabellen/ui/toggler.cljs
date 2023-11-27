(ns matvaretabellen.ui.toggler
  (:require [matvaretabellen.ui.dom :as dom]))

(defn get-target [el]
  (if-let [selector (.getAttribute el "data-toggle-target")]
    [el (dom/qs selector)]
    (some-> el .-parentNode get-target)))

(defn init-toggler [toggler]
  (->> (fn [e]
         (when-let [[el target] (get-target (.-target e))]
           (let [hidden? (dom/has-class target "mmm-hidden")]
             (when (and hidden? (dom/has-class el "mmm-button-secondary"))
               (dom/remove-class el "mmm-button-secondary"))
             (when (and (not hidden?) (dom/has-class el "mmm-button"))
               (dom/add-class el "mmm-button-secondary"))
             (if hidden?
               (dom/show target)
               (dom/hide target)))))
       (.addEventListener toggler "click")))

(defn init []
  (doall (map init-toggler (dom/qsa "[data-toggle-target]"))))
