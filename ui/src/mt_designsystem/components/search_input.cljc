(ns mt-designsystem.components.search-input)

(defn SearchInput [{:keys [label button input results autocomplete-id]}]
  [:form.form-layout
   [:label.form-label {:for (:name input)} label]
   [:div.mvt-autocomplete
    [:div.search-wrap
     [:input.form-field.input-search.hasButton
      (cond-> {:id (:name input)
               :name (:name input)
               :type "search"}
        autocomplete-id
        (assoc :aria-autocomplete "list"
               :autocomplete "off"
               :aria-controls autocomplete-id
               :aria-haspopup "menu"))]
     [:button.button.button--flat.form-field.button-search-primary.icon--search-before-beige
      {:type "submit"} (:text button)]]
    (when (seq results)
      [:ol.mvt-ac-results
       (for [result results]
         [:li.mvt-ac-result
          [:a {:href (:href result)} (:text result)]])])]])
