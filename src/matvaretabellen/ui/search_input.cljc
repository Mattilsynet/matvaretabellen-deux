(ns matvaretabellen.ui.search-input
  (:require [mattilsynet.design :as m]))

(def size-classes
  {:small "sm"})

(defn SearchInput [{:keys [button input results size class default-value]}]
  [:div {:class (m/c :flex class)
         :data-items "100"
         :data-nowrap ""
         :data-size (size-classes size)}
   [:div {:class (m/c :field)}
    [:u-combobox
     {:data-creatable ""}
     [:input (assoc input :type "search" :class (m/c :input))]
     (when default-value
       [:u-datalist
        {:data-nofilter ""}
        [:u-option {:value ""} default-value]
        (for [result results]
          [:u-option
           (when (:selected? result)
             {:aria-selected "true"})
           [:a {:href (:href result)}
            (:text result)]])])]]
   [:button {:class (m/c :button)
             :type "submit"
             :data-fixed ""
             :data-variant "primary"}
    (:text button)]])
