(ns mmm.layouts.banner-scenes
  (:require [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene media-buttons-banner
  [:div.mmm-banner-media-buttons
   [:div.mmm-container
    [:div.mmm-media
     [:aside
      [:img.mmm-img {:src "/images/banana.jpg"}]]
     [:article.mmm-text.mmm-tight
      [:h4 "Visste du at..."]
      [:p "Banan er den frukten med høyest innhold av karbohydrater blant
    fruktene i Matvaretabellen. Den har også et høyt innhold av magnesium."]
      [:p [:a {:href "#"} "Les mer om bananen her"]]]]
    [:div.mmm-buttons.mmm-text.mmm-tight
     [:a.mmm-banner-button {:href "/matvaregrupper/"}
      [:p "Alle matvaregrupper →"]
      [:p "Se oversikt over alle matvaregrupper."]]
     [:a.mmm-banner-button {:href "/naeringsstoffer/"}
      [:p "Alle næringsstoffer →"]
      [:p "Se oversikt over alle næringsstoffer."]]]]])

(defscene media-buttons-banner-different-theme
  [:div.mmm-banner-media-buttons.mmm-brand-theme2
   [:div.mmm-container
    [:div.mmm-media
     [:aside
      [:img.mmm-img {:src "/images/banana.jpg"}]]
     [:article.mmm-text.mmm-tight
      [:h4 "Visste du at..."]
      [:p "Banan er den frukten med høyest innhold av karbohydrater blant
    fruktene i Matvaretabellen. Den har også et høyt innhold av magnesium."]
      [:p [:a {:href "#"} "Les mer om bananen her"]]]]
    [:div.mmm-buttons.mmm-text.mmm-tight
     [:a.mmm-banner-button {:href "/matvaregrupper/"}
      [:p "Alle matvaregrupper →"]
      [:p "Se oversikt over alle matvaregrupper."]]
     [:a.mmm-banner-button {:href "/naeringsstoffer/"}
      [:p "Alle næringsstoffer →"]
      [:p "Se oversikt over alle næringsstoffer."]]]]])
