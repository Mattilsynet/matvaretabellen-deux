(ns matvaretabellen.components.search-input
  (:require [matvaretabellen.components.button :refer [Button]]
            [matvaretabellen.components.text-input :refer [TextInput]]))

(defn SearchInput [{:keys [button input results autocomplete-id]}]
  [:fieldset.mvt-search-input
   [:div.mvt-action-input
    (TextInput
     (cond-> {:id (:name input)
              :name (:name input)
              :type "search"}
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
         [:a {:href (:href result)} (:text result)]])])])
