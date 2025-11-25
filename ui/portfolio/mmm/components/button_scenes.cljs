(ns mmm.components.button-scenes
  (:require [mmm.components.button :refer [Button]]
            [mmm.elements :as e]
            [phosphor.icons :as icons]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Knapp"})

(defscene regular-button
  (e/p
   (Button {:text "Trykk på knappen"})))

(defscene link-button
  (e/p
   (Button {:text "Trykk på knappen"
            :href "/hello"})))

(defscene inline-button
  (e/p
   (Button {:text "Trykk på knappen"
            :href "/hello"
            :inline? true})))

(defscene secondary-button
  (e/p
   (Button {:text "Trykk på knappen"
            :href "/hello"
            :secondary? true
            :inline? true})))

(defscene button-with-icon
  (e/p
   (Button {:text "Trykk på knappen"
            :href "/hello"
            :icon (icons/icon :phosphor.regular/magnifying-glass)})))

(defscene inline-button-with-icon
  (e/p
   (Button {:text "Trykk på knappen"
            :href "/hello"
            :icon (icons/icon :phosphor.regular/magnifying-glass)
            :inline? true})))

(defscene secondary-inline-button-with-icon
  (e/p
   (Button {:text "Trykk på knappen"
            :href "/hello"
            :icon (icons/icon :phosphor.regular/arrow-down)
            :inline? true
            :secondary? true})))

(defscene big-button-with-icon
  (e/p
   (Button {:text "Trykk på knappen"
            :href "/hello"
            :icon (icons/icon :phosphor.regular/magnifying-glass)
            :inline? true
            :size :large})))
