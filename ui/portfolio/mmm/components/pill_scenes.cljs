(ns mmm.components.pill-scenes
  (:require [fontawesome.icons :as icons]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Piller"})

(defscene basic-pill
  [:a.mmm-pill.mmm-actionable
   (icons/render (icons/icon :fontawesome.solid/x) {:class :mmm-svg})
   "Pilletekst"])

(defscene list-of-pills
  [:ul.mmm-ul.mmm-horizontal-list.mmm-pills
   [:li
    [:a.mmm-pill.mmm-actionable
     (icons/render (icons/icon :fontawesome.solid/x) {:class :mmm-svg})
     "Brød"]]
   [:li
    [:a.mmm-pill.mmm-actionable
     (icons/render (icons/icon :fontawesome.solid/x) {:class :mmm-svg})
     "Epler"]]
   [:li
    [:a.mmm-pill.mmm-actionable
     (icons/render (icons/icon :fontawesome.solid/x) {:class :mmm-svg})
     "Bananer"]]
   [:li
    [:a.mmm-pill.mmm-actionable
     (icons/render (icons/icon :fontawesome.solid/x) {:class :mmm-svg})
     "Piller"]]])
