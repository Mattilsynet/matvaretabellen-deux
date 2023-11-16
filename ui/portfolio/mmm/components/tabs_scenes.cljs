(ns mmm.components.tabs-scenes
  (:require [mmm.components.tabs :refer [Tabs]]
            [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene two-tabs
  (e/block
   (Tabs
    {:tabs [{:text "Kakediagram" :selected? true}
            {:text "Tabell"}]})))

(defscene three-tabs
  (e/block
   (Tabs
    {:tabs [{:text "Kakediagram" :selected? true}
            {:text "Tabell"}
            {:text "Banan"}]})))
