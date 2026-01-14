(ns matvaretabellen.ui.toggler
  (:require [matvaretabellen.ui.dom :as dom]))

(defn get-target [el]
  (if-let [selector (.getAttribute el "data-toggle-target")]
    [el (dom/qs selector)]
    (some-> el .-parentNode get-target)))

(defn toggle [el selected?]
  (when-not (dom/has-attr? el "data-original-variant")
    (dom/set-attr el "data-original-variant" (dom/get-attr el "data-variant")))
  (dom/set-attr el "data-variant"
                (if selected?
                  "primary"
                  (dom/get-attr el "data-original-variant"))))

(defn init-toggler [toggler]
  (->> (fn [e]
         (when-let [[el target] (get-target (.-target e))]
           (let [hidden? (dom/hidden? target)]
             (toggle el (dom/hidden? target))

             ;; Show/hide
             (if hidden?
               (dom/show target)
               (dom/hide target)))))
       (.addEventListener toggler "click")))

(defn init []
  (doall (map init-toggler (dom/qsa "[data-toggle-target]"))))
