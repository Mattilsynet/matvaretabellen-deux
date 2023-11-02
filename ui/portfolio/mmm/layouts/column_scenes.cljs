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

(defscene three-column-layouts
  ".mmm-threecol gir en layout med tre kolonner hvor den midterste får mest plass"
  [:div.mmm-threecol
   [:div.mmm-col
    (e/h3 "Jeg er støtteinnhold")
    (e/p "Det kan være nyttig, det, men er ikke akkurat det viktigste.")]
   [:div.mmm-col
    (e/h2 "Det viktigste innholdet, det er meg")
    (e/p "I midten setter vi av ekstra plass til den aller mest prominente
    seksjonen, selve blikkfanget, om du vil.")]
   [:div.mmm-col
    (e/h3 "Jeg er mer støtteinnhold")
    (e/p "Det kan være nyttig, det, men er ikke akkurat det viktigste.")]])
