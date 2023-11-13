(ns mmm.components.icon-button-scenes
  (:require [fontawesome.icons :as icons]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Ikonknapper"})

(defscene icon-button
  [:span.mmm-icon-button.mmm-actionable
   {:title "Sammenlign"}
   (icons/render (icons/icon :fontawesome.solid/code-compare) {:class :mmm-svg})])

(defscene active-icon-button
  [:span.mmm-icon-button.mmm-actionable.mmm-icon-button-active
   {:title "Sammenlign"}
   (icons/render (icons/icon :fontawesome.solid/code-compare) {:class :mmm-svg})])

(defscene medium-icon-buttons
  [:ul.mmm-horizontal-list
   [:li
    [:span.mmm-icon-button.mmm-actionable.mmm-icon-button-m
     {:title "Sammenlign"}
     (icons/render (icons/icon :fontawesome.solid/code-compare) {:class :mmm-svg})]]
   [:li
    [:span.mmm-icon-button.mmm-actionable.mmm-icon-button-m.mmm-icon-button-active
     {:title "Sammenlign"}
     (icons/render (icons/icon :fontawesome.solid/code-compare) {:class :mmm-svg})]]])
