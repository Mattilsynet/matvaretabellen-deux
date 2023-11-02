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

(defscene cards-with-block-content
  [:div.mmm-cards
   (for [[title detail] [["Fettsyrer →" "14 g"]
                         ["Proteiner →" "20 g"]
                         ["Karbohydrater →" "0 g"]]]
     [:a.mmm-card.mmm-link.mmm-text {:href "#"}
      [:h3 title]
      [:p [:strong detail]]])])
