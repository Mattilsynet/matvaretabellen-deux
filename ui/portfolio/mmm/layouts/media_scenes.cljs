(ns mmm.layouts.media-scenes
  (:require [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene media
  (e/block
   [:div.mmm-media
    [:aside
     [:img.mmm-img {:src "/images/banana.jpg"}]]
    [:article.mmm-text.mmm-tight
     [:h4 "Visste du at..."]
     [:p "Banan er den frukten med høyest innhold av karbohydrater blant
    fruktene i Matvaretabellen. Den har også et høyt innhold av magnesium."]
     [:p [:a {:href "#"} "Les mer om bananen her"]]]]))
