(ns matvaretabellen.ui.food-page
  (:require [matvaretabellen.ui.dom :as dom]))

(defn set-up-tabs []
  (let [piechart-segment (dom/by-id "piechart-segment")
        piechart-display (dom/by-id "piechart-display")
        table-segment (dom/by-id "table-segment")
        table-display (dom/by-id "table-display")]
    (when (and piechart-segment piechart-display
               table-segment table-display)
      (.addEventListener
       table-segment
       "click"
       (fn [_e]
         (.add (.-classList piechart-display) "mmm-hidden")
         (.remove (.-classList table-display) "mmm-hidden")

         (.remove (.-classList piechart-segment) "selected")
         (.add (.-classList table-segment) "selected")))

      (.addEventListener
       piechart-segment
       "click"
       (fn [_e]
         (.add (.-classList table-display) "mmm-hidden")
         (.remove (.-classList piechart-display) "mmm-hidden")

         (.remove (.-classList table-segment) "selected")
         (.add (.-classList piechart-segment) "selected"))))))
