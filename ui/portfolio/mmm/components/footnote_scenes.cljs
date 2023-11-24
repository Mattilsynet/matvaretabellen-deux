(ns mmm.components.footnote-scenes
  (:require [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Fotnoter"})

(defscene footnote
  [:div.mmm-footnote.mmm-small.mvtc-suggestions.mmm-inline
   [:strong "Forslag: "]
   [:ul.mmm-ul.mmm-horizontal-list.mmm-hl-divider
    [:li [:a.mmm-link {:href "#"} "Fisk med bein"]]
    [:li [:a.mmm-link {:href "#"} "Fisk uten bein"]]
    [:li [:a.mmm-link {:href "#"} "Fisk med føtter"]]
    [:li [:a.mmm-link {:href "#"} "Fisk med armer"]]
    [:li [:a.mmm-link {:href "#"} "Fisk med vinger"]]]])
