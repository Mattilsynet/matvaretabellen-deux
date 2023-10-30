(ns mmm.layouts.container-scenes
  (:require [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene container
  ".mmm-container begrenser bredden på elementene til design-tokens for grid.
  Denne størrelsen er ulik for desktop og mobil."
  [:div.mmm-container
   (e/h1 "Jeg er i en container")
   (e/p "Containeren sørger for at ikke innholdet renner over alle bauger og
   kanter, og blir så bredt at det blir vanskelig å finne noen struktur. Denne
   layouten brukes typisk ytterst for å sette rammene for hele sidens bredde, og
   er ikke nødvendigvis egnet til å ha mye tekst i (da det fort blir i det
   bredeste laget).")
   (e/p "Under ser du en hund som illustrerer bolkens bredde.")
   (e/p (e/img {:src "/images/dog.jpg"}))])
