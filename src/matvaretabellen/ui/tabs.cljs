(ns matvaretabellen.ui.tabs
  (:require [matvaretabellen.ui.dom :as dom]))

(defn get-target [el]
  (if-let [selector (some-> el (.getAttribute "data-tab-target"))]
    [el (dom/qs selector)]
    (some-> el .-parentNode get-target)))

(defn select-tab [tab]
  (when-let [[el target] (get-target tab)]
    (when-not (dom/has-class el "selected")
      (when-let [[current current-target] (some-> (.-parentNode el)
                                                  (dom/qs ".selected.tab")
                                                  get-target)]
        (dom/remove-class current "selected")
        (dom/hide current-target))
      (dom/add-class el "selected")
      (dom/show target))))

(defn get-tab [target-selector]
  (dom/qs (str ".mmm-tabs .tab[data-tab-target='" target-selector "']")))

(defn init-tabs [tabs]
  (when (dom/qs tabs "[data-tab-target]")
    (->> (fn [e]
           (select-tab (.-target e)))
         (.addEventListener tabs "click"))))

(defn init []
  (doall (map init-tabs (dom/qsa ".mmm-tabs"))))
