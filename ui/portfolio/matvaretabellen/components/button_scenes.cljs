(ns matvaretabellen.components.button-scenes
  (:require [mt-designsystem.components.button :as b]
            [mt-designsystem.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene regular-button
  (e/p
   (b/button {:text "Trykk på knappen"})))

(defscene link-button
  (e/p
   (b/button {:text "Trykk på knappen"
              :href "/hello"})))

(defscene inline-button
  (e/p
   (b/button {:text "Trykk på knappen"
              :href "/hello"
              :inline? true})))
