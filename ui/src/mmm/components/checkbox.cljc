;; TODO EIRIK: Not in use anymore
(ns mmm.components.checkbox
  (:require [mattilsynet.design :as mtds]))

(defn Checkbox [{:keys [checked? label] :as attrs}]
  [:div {:class (mtds/classes :field)}
   [:input {:class (mtds/classes :input) :type "checkbox" :checked (when checked? "true")}]
   [:label (dissoc attrs :checked? :label)
    label]
   ])
