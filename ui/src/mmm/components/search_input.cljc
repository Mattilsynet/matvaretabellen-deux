(ns mmm.components.search-input
  (:require [mmm.components.button :refer [Button]]
            [mmm.components.text-input :refer [TextInput]]))

(defn SearchInput [{:keys [button input results autocomplete-id]}]
  [:fieldset.mmm-search-input
   [:div.mmm-action-input
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
     [:ol.mmm-ac-results
      (for [result results]
        [:li.mmm-ac-result
         (when (:selected? result)
           {:class "mmm-ac-selected"})
         [:a {:href (:href result)}
          (:text result)]])])])
