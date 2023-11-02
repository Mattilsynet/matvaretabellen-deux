(ns mmm.layouts.column-scenes
  (:require [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene column-layouts
  ".mmm-cols gir en layout med kolonner a minimum 200px, med wrap. Kan brukes
  når du ønsker en kolonnelayout med kolonner som er like store."
  [:div.mmm-cols
   [:div.mmm-col
    (e/h3 "Jeg er noe innhold")
    (e/p "Det kan være nyttig, det, minst like viktig som det andre innholdet.")]
   [:div.mmm-col
    (e/h3 "Jeg er noe innhold")
    (e/p "Det kan være nyttig, det, minst like viktig som det andre innholdet.")]
   [:div.mmm-col
    (e/h3 "Jeg er noe innhold")
    (e/p "Det kan være nyttig, det, minst like viktig som det andre innholdet.")]
   [:div.mmm-col
    (e/h3 "Jeg er noe innhold")
    (e/p "Det kan være nyttig, det, minst like viktig som det andre innholdet.")]
   [:div.mmm-col
    (e/h3 "Jeg er noe innhold")
    (e/p "Det kan være nyttig, det, minst like viktig som det andre innholdet.")]
   [:div.mmm-col
    (e/h3 "Jeg er noe innhold")
    (e/p "Det kan være nyttig, det, minst like viktig som det andre innholdet.")]])
