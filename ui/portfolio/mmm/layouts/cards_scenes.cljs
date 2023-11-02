(ns mmm.layouts.cards-scenes
  (:require [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Kort"})

(defscene cards
  [:div.mmm-cards.mmm-block
   (for [text ["Sukker og søte produkter"
               "Margarin, smør, matolje o.l."
               "Drikke"
               "Alle matvarer"
               "Diverse retter, produkter og ingredienser"
               "Spedbarnsmat"
               "Melk og melkeprodukter"]]
     [:a.mmm-card.mmm-link {:href "#"}
      text])])
