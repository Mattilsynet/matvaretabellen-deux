(ns matvaretabellen.elements.typography-scenes
  (:require [matvaretabellen.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene headings
  "Frittstående headings styles med klassenavn, eksempelvis .mvt-h1"
  [:div
   (e/h1 "Dette er en h1!")
   (e/h2 "Dette er en h2")
   (e/h3 "Dette er en h3")
   (e/h4 "Dette er en h4")
   (e/h5 "Dette er en h5")
   (e/h6 "Dette er en h6")])

(defscene paragraph
  (e/p "Dette er en frittstående paragraf, den får sin styling ved å bruke
   klassenavnet .mvt-p. Da blir det fart i sakene og tak i typografien. Mye kan
   sies om den helt vanlige paragrafen. Men det er en tid for alt. Så joda, så
   neida."))

(defscene link
  (e/p [:a.mvt-link {:href "#"} "En frittstående lenke med .mvt-link"]))

(defscene fit-to-content
  [:div
   (e/p {:class :mvt-fc} "Denne paragrafen er ikke så bred")
   (e/p "Paragrafen over bruker .mvt-fc som gir width: fit-content.")])

(defscene unordered-list
  (e/ul
   [:li "Dette er en punktliste"]
   [:li "Den kan ha mange punkter"]
   [:li "Det er kun fantasien som setter grenser"]
   [:li "Frittstående lister får styles fra .mvt-ul"]))

(defscene ordered-list
  (e/ol
   [:li "Dette er en nummerert liste"]
   [:li "Den kan ha mange punkter"]
   [:li "Det er kun fantasien som setter grenser"]
   [:li "Frittstående lister får styles fra .mvt-ol"]))

(defscene unadorned-list
  (e/ul {:class "mvt-unadorned-list"}
   [:li "Dette er en unummerert liste"]
   [:li "Den kan ha mange punkter"]
   [:li "Det er kun fantasien som setter grenser"]
   [:li "Siden den er \"unadorned\" har den ingen kulepunkter"]))

(defscene right-aligned-text
  (e/p {:class "mvt-tar"}
       "Denne paragrafen har høyrejustert tekst, takket være klassen
       .mvt-tar (\"text align right\"). En grunnleggende byggekloss vi
       vil få bruk for i ny og ne."))

(defscene centered-text
  (e/p {:class "mvt-tac"}
       "Denne paragrafen har sentrert tekst, takket være klassen
       .mvt-tac (\"text align center\"). En veldig grunnleggende byggekloss vi
       vil få bruk for stadig vekk."))

(defscene no-break
  (e/p {:class "mvt-nbr"}
       "Denne teksten bruker klassen .mvt-nbr og skal ikke brekkes på flere linjer uansett om den så må overflowe ut på siden. Og ja, blir den lang nok så gjør den akkurat det."))

(defscene preamble
  [:div
   (e/p {:class "mvt-preamble"}
        "Denne paragrafen er en ingress, i kraft av klassen .mvt-preamble.")
   (e/p {:class "mvt-preamble-s"}
        "Denne paragrafen er også en ingress, men en litt mer subtil en. Den
         benytter seg av klassen .mvt-preamble-s.")])

(defscene mt-text
  "`.mvt-text` gir default styling til de viktigste typografiske
  elementene (headinger, paragrafer, lister, osv). Denne klassen er nyttig når
  du ikke har full kontroll på markup, feks når du skal rendre innholdet fra
  rike tekstfelter, HTML oversatt fra markdown osv."
  (e/text
   [:h1 "Dette er en h1"]
   [:p "Og dette er en paragraf med litt tekst."]
   [:p "Denne paragrafen har en " [:a {:href "#"} "lenke"] "."]
   [:h2 "Dette er en h2"]
   [:ul
    [:li "Dette er en punktliste"]
    [:li "Den kan ha mange punkter"]
    [:li "Det er kun fantasien som setter grenser"]]
   [:h3 "Dette er en h3"]
   [:ol
    [:li "Dette er en nummerert liste"]
    [:li "Den kan ha mange punkter"]
    [:li "Det er kun fantasien som setter grenser"]
    [:li "Det bør"]
    [:li "være støtte"]
    [:li "for så"]
    [:li "meget som"]
    [:li "et tosiffret"]
    [:li "antall"]
    [:li "punkter!"]]
   [:h4 "Dette er en h4"]
   [:h5 "Dette er en h5"]
   [:h6 "Dette er en h6"]))
