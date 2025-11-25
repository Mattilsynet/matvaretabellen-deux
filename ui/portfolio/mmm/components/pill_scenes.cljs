(ns mmm.components.pill-scenes
  (:require [phosphor.icons :as icons]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Piller"})

(defscene basic-pill
  [:a.mmm-pill.mmm-actionable
   "Pilletekst"
   (icons/render (icons/icon :phosphor.regular/x) {:class :mmm-svg})])

(defscene list-of-pills
  [:ul.mmm-ul.mmm-horizontal-list.mmm-pills
   [:li
    [:a.mmm-pill.mmm-actionable
     "Brød"
     (icons/render (icons/icon :phosphor.regular/x) {:class :mmm-svg})]]
   [:li
    [:a.mmm-pill.mmm-actionable
     "Epler"
     (icons/render (icons/icon :phosphor.regular/x) {:class :mmm-svg})]]
   [:li
    [:a.mmm-pill.mmm-actionable
     "Bananer"
     (icons/render (icons/icon :phosphor.regular/x) {:class :mmm-svg})]]
   [:li
    [:a.mmm-pill.mmm-actionable
     "Piller"
     (icons/render (icons/icon :phosphor.regular/x) {:class :mmm-svg})]]])
