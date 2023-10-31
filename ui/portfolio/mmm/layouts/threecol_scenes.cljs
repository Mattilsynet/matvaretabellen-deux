(ns mmm.layouts.threecol-scenes
  (:require [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

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
