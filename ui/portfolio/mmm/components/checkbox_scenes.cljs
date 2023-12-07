(ns mmm.components.checkbox-scenes
  (:require [mmm.components.checkbox :refer [Checkbox]]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Checkbox"})

(defscene checkbox
  (Checkbox
   {:checked? false
    :label "Checkbox"}))

(defscene checkbox-checked
  (Checkbox
   {:checked? true
    :label "Checkbox"}))

(defscene small-checked
  [:div.mmm-small
   (Checkbox
    {:checked? true
     :label "Checkbox"})])
