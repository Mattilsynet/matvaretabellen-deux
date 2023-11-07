(ns matvaretabellen.components.cols-2-1-labeled-scenes
  (:require [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene cols-2-1-labeled
  "En layout temmelig skreddersydd for å vise to kakediagrammer med en legend ved siden av."
  [:div.mmm-flex-gap-huge.mvt-cols-2-1-labeled
   [:div.col-2
    [:div.label [:h3.mmm-h3 "Overskrift"]]
    [:div {:style {:background "blue" :height "30vw"}}]]
   [:div.col-2
    [:div.label [:h3.mmm-h3 "Overskrift"]]
    [:div {:style {:background "red" :height "30vw"}}]]
   [:div.col-1
    "Noe annet innhold her"]])
