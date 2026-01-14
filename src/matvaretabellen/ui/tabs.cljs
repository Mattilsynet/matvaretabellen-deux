(ns matvaretabellen.ui.tabs
  (:require [matvaretabellen.ui.dom :as dom]))

(defn get-target [el]
  (some-> el .-value dom/qs))

(defn select-tab [tab]
  (when-let [target (get-target tab)]
    (doseq [current-target (->> (dom/qsa "input[name=\"comparison-view\"]:not(:checked)")
                                (map get-target))]
      (dom/hide current-target))
    (dom/show target)))

(defn get-tab [target-selector]
  (dom/qs (str "input[name=\"comparison-view\"][value='" target-selector "']")))

(defn init-tabs []
  (doseq [tab (dom/qsa "input[name=\"comparison-view\"]")]
    (->> (fn [e]
           (select-tab (.-target e)))
         (.addEventListener tab "change"))))

(defn init []
  (init-tabs))
