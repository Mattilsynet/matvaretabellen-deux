(ns mmm.layouts.media-scenes
  (:require [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Media"})

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

(defscene media-anchor-top
  "`.mmm-media.mmm-media-at` ankrer teksten mot toppen av media-komponenten, i
  stedet for midten."
  (e/block
   [:div.mmm-media.mmm-media-at
    [:aside
     [:img.mmm-img {:src "/images/banana.jpg"}]]
    [:article.mmm-text.mmm-tight
     [:h4 "Visste du at..."]
     [:p "Banan er den frukten med høyest innhold av karbohydrater blant
    fruktene i Matvaretabellen. Den har også et høyt innhold av magnesium."]
     [:p [:a {:href "#"} "Les mer om bananen her"]]]]))

(defscene media-stamp
  "`.mmm-media.mmm-media-stamp` flytter `aside` til å flyte som et lite frimerke
  på smale skjermer, for å gi mer rom. Du må selv sette ønsket bredde."
  (e/block
   [:div.mmm-media.mmm-media-stamp
    [:aside {:style {:width "30%"}}
     [:img.mmm-img {:src "/images/banana.jpg"}]]
    [:article.mmm-text.mmm-tight
     [:h4 "Visste du at..."]
     [:p "Banan er den frukten med høyest innhold av karbohydrater blant
    fruktene i Matvaretabellen. Den har også et høyt innhold av magnesium."]
     [:p [:a {:href "#"} "Les mer om bananen her"]]]]))
