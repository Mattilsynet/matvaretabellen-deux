(ns mmm.components.search-input
  (:require [mattilsynet.design :as mtds]))

(def size-classes
  {:small "sm"})

(defn SearchInput [{:keys [button input results size class default-value]}]
  [:div {:class (mtds/classes :flex class)
         :data-items "100"
         :data-nowrap ""
         :data-size (size-classes size)}
   [:div {:class (mtds/classes :field)}
    [:u-combobox
     {:data-creatable ""}
     [:input (assoc input :type "search" :class (mtds/classes :input))]
     [:u-datalist
      {:data-nofilter ""}
      [:u-option {:value ""} default-value]
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
