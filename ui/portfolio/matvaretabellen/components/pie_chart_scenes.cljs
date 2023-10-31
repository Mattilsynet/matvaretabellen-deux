(ns matvaretabellen.components.pie-chart-scenes
  (:require [matvaretabellen.components.pie-chart :refer [PieChart assoc-degrees]]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene pie-chart-1
  [:div {:style {:width 200}}
   (PieChart {:slices [{:from-deg 0 :to-deg 60    :value 10 :color "red"}
                       {:from-deg 60 :to-deg 180  :value 20 :color "orange"}
                       {:from-deg 180 :to-deg 360 :value 30 :color "blue"}]})])

(defscene pie-chart-2
  [:div {:style {:width 200}}
   (PieChart {:slices (assoc-degrees
                       10
                       [{:value 13 :color "red"}
                        {:value 43 :color "orange"}
                        {:value 22 :color "blue"}
                        {:value 30 :color "green"}
                        {:value 5 :color "purple"}])})])
