(ns mmm.elements.typography-scenes
  (:require [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Typografi"})

(defscene headings
  "Frittstående headings styles med klassenavn, eksempelvis .mmm-h1"
  [:div.mmm-vert-layout-m
   (e/h1 "Dette er en h1!")
   (e/h2 "Dette er en h2")
   (e/h3 "Dette er en h3")
   (e/h4 "Dette er en h4")
   (e/h5 "Dette er en h5")
   (e/h6 "Dette er en h6")])

(defscene paragraph
  (e/p "Dette er en frittstående paragraf, den får sin styling ved å bruke
   klassenavnet .mmm-p. Da blir det fart i sakene og tak i typografien. Mye kan
   sies om den helt vanlige paragrafen. Men det er en tid for alt. Så joda, så
   neida."))

(defscene link
  (e/p [:a.mmm-link {:href "#"} "En frittstående lenke med .mmm-link"]))

(defscene fit-to-content
  [:div
   (e/p {:class :mmm-fc} "Denne paragrafen er ikke så bred")
   (e/p "Paragrafen over bruker .mmm-fc som gir width: fit-content.")])

(defscene unordered-list
  (e/ul
   [:li "Dette er en punktliste"]
   [:li "Den kan ha mange punkter"]
   [:li "Det er kun fantasien som setter grenser"]
   [:li "Frittstående lister får styles fra .mmm-ul"]))

(defscene ordered-list
  (e/ol
   [:li "Dette er en nummerert liste"]
   [:li "Den kan ha mange punkter"]
   [:li "Det er kun fantasien som setter grenser"]
   [:li "Frittstående lister får styles fra .mmm-ol"]))

(defscene unadorned-list
  (e/ul {:class "mmm-unadorned-list"}
   [:li "Dette er en unummerert liste"]
   [:li "Den kan ha mange punkter"]
   [:li "Det er kun fantasien som setter grenser"]
   [:li "Siden den er \"unadorned\" har den ingen kulepunkter"]))

(defscene right-aligned-text
  (e/p {:class "mmm-tar"}
       "Denne paragrafen har høyrejustert tekst, takket være klassen
       .mmm-tar (\"text align right\"). En grunnleggende byggekloss vi
       vil få bruk for i ny og ne."))

(defscene centered-text
  (e/p {:class "mmm-tac"}
       "Denne paragrafen har sentrert tekst, takket være klassen
       .mmm-tac (\"text align center\"). En veldig grunnleggende byggekloss vi
       vil få bruk for stadig vekk."))

(defscene no-break
  (e/p {:class "mmm-nbr"}
       "Denne teksten bruker klassen .mmm-nbr og skal ikke brekkes på flere linjer uansett om den så må overflowe ut på siden. Og ja, blir den lang nok så gjør den akkurat det."))

(defscene preamble
  [:div.mmm-vert-layout-m
   (e/p {:class "mmm-preamble"}
        "Denne paragrafen er en ingress, i kraft av klassen .mmm-preamble.")
   (e/p {:class "mmm-preamble-s"}
        "Denne paragrafen er også en ingress, men en litt mer subtil en. Den
         benytter seg av klassen .mmm-preamble-s.")])

(defscene mmm-text
  "`.mmm-text` gir default styling til de viktigste typografiske
  elementene (headinger, paragrafer, lister, osv). Denne klassen er nyttig når
  du ikke har full kontroll på markup, feks når du skal rendre innholdet fra
  rike tekstfelter, HTML oversatt fra markdown osv. Kombinert med
  `.mmm-vert-layout-m` får elementene fin vertikal spacing."
  (e/text {:class "mmm-vert-layout-m"}
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

(defscene mmm-tight
  "`.mmm-text.mmm-vert-layout-s` fungerer som over men med mindre vertikal
  spacing."
  (e/text {:class "mmm-vert-layout-s"}
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
