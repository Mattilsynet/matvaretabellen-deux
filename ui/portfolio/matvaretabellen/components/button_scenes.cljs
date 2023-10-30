(ns matvaretabellen.components.button-scenes
  (:require [fontawesome.icons :as icons]
            [matvaretabellen.components.button :refer [Button]]
            [matvaretabellen.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

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

(defscene button-with-icon
  (e/p
   (Button {:text "Trykk på knappen"
            :href "/hello"
            :icon (icons/icon :fontawesome.solid/magnifying-glass)})))

(defscene inline-button-with-icon
  (e/p
   (Button {:text "Trykk på knappen"
            :href "/hello"
            :icon (icons/icon :fontawesome.solid/magnifying-glass)
            :inline? true})))

(defscene big-button-with-icon
  (e/p
   (Button {:text "Trykk på knappen"
            :href "/hello"
            :icon (icons/icon :fontawesome.solid/magnifying-glass)
            :inline? true
            :size :large})))
