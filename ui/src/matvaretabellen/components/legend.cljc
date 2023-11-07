(ns matvaretabellen.components.legend)

(defn Legend [{:keys [entries]}]
  [:div.mvt-legend
   (for [entry entries]
     [:div.legend-entry
      [:div.legend-color {:style {:background-color (:color entry)}}]
      [:div.legend-label (:label entry)]])])
