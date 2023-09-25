(ns matvaretabellen.components.nutrition-table)

(defn NutritionTable [{:keys [title class subtitle categories]}]
  [:div.nutrition-table {:class class}
   [:table
    [:caption title]
    [:thead
     [:tr
      [:th {:colspan 2} subtitle]]]
    [:tbody
     (mapcat
      (fn [{:keys [label content subcategories]}]
        [[:tr
          [:th label]
          [:td content]]
         (for [{:keys [label content]} subcategories]
           [:tr.subcategory
            [:th label]
            [:td content]])])
      categories)]]])
