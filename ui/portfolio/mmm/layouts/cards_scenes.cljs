(ns mmm.layouts.cards-scenes
  (:require [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Kort"})

(defscene cards
  [:div.mmm-cards
   (for [text ["Sukker og søte produkter"
               "Margarin, smør, matolje o.l."
               "Drikke"
               "Alle matvarer"
               "Diverse retter, produkter og ingredienser"
               "Spedbarnsmat"
               "Melk og melkeprodukter"]]
     [:a.mmm-card.mmm-link {:href "#"}
      text])])

(def macros
  [["Fettsyrer →" "14 g"]
   ["Proteiner →" "20 g"]
   ["Karbohydrater →" "0 g"]])

(defscene cards-with-block-content-1
  [:div.mmm-cards
   (for [[title detail] macros]
     [:a.mmm-card.mmm-link.mmm-text.mmm-vert-layout-m {:href "#"}
      [:h3.mmm-nbr title]
      [:p [:strong detail]]])])

(defscene cards-with-block-content-2
  [:div.mmm-cards
   (for [[title detail] macros]
     [:a.mmm-card.mmm-link.mmm-text.mmm-vert-layout-s {:href "#"}
      [:p.mmm-nbr title]
      [:h2 detail]])])
