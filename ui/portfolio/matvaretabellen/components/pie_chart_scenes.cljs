(ns matvaretabellen.components.pie-chart-scenes
  (:require [matvaretabellen.components.pie-chart :refer [assoc-degrees PieChart]]
            [matvaretabellen.ui.hoverable :as hoverable]
            [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene basic-pie-chart
  (e/block
   [:div {:style {:width 300}}
    (PieChart {:slices [{:from-deg 0 :to-deg 60    :value 10 :color "red"}
                        {:from-deg 60 :to-deg 180  :value 20 :color "orange"}
                        {:from-deg 180 :to-deg 360 :value 30 :color "blue"}]})]))

(defscene pie-chart-with-hover
  (e/block
   [:div {:style {:width 300}
          :ref hoverable/set-up}
    (PieChart {:slices (assoc-degrees
                        10
                        [{:value 13 :color "red"    :hover-content [:span "Red: " [:strong "13 g"]]}
                         {:value 43 :color "orange" :hover-content [:span "Orange: " [:strong "43 g"]]}
                         {:value 22 :color "blue"   :hover-content [:span "Blue: " [:strong "22 g"]]}
                         {:value 30 :color "green"  :hover-content [:span "Green: " [:strong "30 g"]]}
                         {:value 5  :color "purple" :hover-content [:span "Purple: " [:strong "5 g"]]}])
               :hoverable? true})]))

(defscene pie-chart-with-one-very-large-slice
  (e/block
   [:div {:style {:width 300}}
    (PieChart {:slices (assoc-degrees
                        10
                        [{:id "Fett", :value 10, :color "red", :hover-content [:span {:data-portion 0.1} 0.1]}
                         {:id "Karbo", :value 120, :color "green", :hover-content [:span {:data-portion 1.2} 1.2]}
                         {:id "Protein", :value 80, :color "yellow", :hover-content [:span {:data-portion 0.8} 0.8]}
                         {:id "Vann", :value 9700, :color "blue", :hover-content [:span {:data-portion 97.0} 97.0]}
                         {:id "Fiber", :value 80, :color "purple", :hover-content [:span {:data-portion 0.8} 0.8]}])})]))

(defscene pie-chart-with-only-one-slice
  (e/block
   [:div {:style {:width 300}}
    (PieChart {:slices (assoc-degrees
                        10
                        [{:id "Karbo", :value 120, :color "green", :hover-content [:span {:data-portion 1.2} 1.2]}])})]))
