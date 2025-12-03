(ns mmm.components.search-input
  (:require [mattilsynet.design :as mtds]))

(def size-classes
  {:small "sm"})

(defn SearchInput [{:keys [button input results autocomplete-id size class]}]
  [:div {:class (mtds/classes :flex class)
         :data-items "100"
         :data-nowrap ""
         :data-size (size-classes size)}
   [:div {:class (mtds/classes :field)}
    [:input
     (cond-> (assoc input :type "search" :class (mtds/classes :input))
       (:name input)
       (assoc :id (:name input))
       autocomplete-id
       (assoc :list autocomplete-id))]
    [:u-combobox
     [:u-datalis
      {:id autocomplete-id}
      (for [result results]
        [:u-option
         (when (:selected? result)
           {:aria-selected "true"})
         [:a {:href (:href result)}
          (:text result)]])]]]
    [:button {:class (mtds/classes :button)
              :type "submit"
              :data-fixed ""
              :data-variant "primary"}
     (:text button)]])
