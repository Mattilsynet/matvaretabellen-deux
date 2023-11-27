(ns mmm.components.search-input
  (:require [mmm.components.button :refer [Button]]
            [mmm.components.text-input :refer [TextInput]]))

(def size-classes
  {:small :mmm-search-input-compact})

(defn SearchInput [{:keys [button input results autocomplete-id size class]}]
  [:fieldset.mmm-search-input {:class [(size-classes size) class]}
   [:div.mmm-action-input
    (TextInput
     (cond-> (assoc input :type "search" :size size :autocomplete "off")
       (:name input)
       (assoc :id (:name input))

       (and autocomplete-id
            (seq results))
       (assoc :aria-autocomplete "list"
              :autocomplete "off"
              :aria-controls autocomplete-id
              :aria-haspopup "menu")))
    (Button (assoc button :type "submit" :inline? true :size size))]
   (when (seq results)
     [:ol.mmm-ac-results {:id autocomplete-id}
      (for [result results]
        [:li.mmm-ac-result
         (when (:selected? result)
           {:class "mmm-ac-selected"})
         [:a {:href (:href result)}
          (:text result)]])])])
