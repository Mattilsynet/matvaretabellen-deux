(ns matvaretabellen.components.search-input
  (:require [matvaretabellen.components.button :refer [Button]]
            [matvaretabellen.components.text-input :refer [TextInput]]))

(defn SearchInput [{:keys [button input results autocomplete-id]}]
  [:fieldset.mvt-search-input
   [:div.mvt-action-input
    (TextInput
     (cond-> (assoc input :type "search")
       (:name input)
       (assoc :id (:name input))

       autocomplete-id
       (assoc :aria-autocomplete "list"
              :autocomplete "off"
              :aria-controls autocomplete-id
              :aria-haspopup "menu")))
    (Button (assoc button :type "submit" :inline? true))]
   (when (seq results)
     [:ol.mvt-ac-results
      (for [result results]
        [:li.mvt-ac-result
         (when (:selected? result)
           {:class "mvt-ac-selected"})
         [:a {:href (:href result)}
          (:text result)]])])])
