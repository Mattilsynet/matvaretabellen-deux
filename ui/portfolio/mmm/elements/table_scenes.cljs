(ns mmm.elements.table-scenes
  (:require [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Tabell"})

(defscene table
  "En standard tabell med .mmm-table"
  [:table.mmm-table
   [:caption "Fettsyrer"]
   [:thead
    [:tr
     [:th "Fettsyrer, sammensetning"]
     [:th.mmm-tar.mmm-table-auto "Mengde (g)"]]]
   [:tbody
    [:tr
     [:th "Fettsyrer, total"]
     [:td.mmm-tar "27,0"]]
    [:tr
     [:th "Mettet fett, total"]
     [:td.mmm-tar "17,1"]]
    [:tr
     [:th "Enumettet fett, total"]
     [:td.mmm-tar "6,5"]]
    [:tr
     [:th "Flerumettet fett, total"]
     [:td.mmm-tar "0,6"]]
    [:tr
     [:th "Transfett"]
     [:td.mmm-tar "0,8"]]
    [:tr
     [:th "Kolestrol"]
     [:td.mmm-tar "0,07"]]]])

(defscene zebra-striped-table
  "Ved å legge .mmm-table-zebra på en standard tabell blir like stripete som en
  av savannens flotteste equidae"
  [:table.mmm-table.mmm-table-zebra
   [:caption "Fettsyrer"]
   [:thead
    [:tr
     [:th "Fettsyrer, sammensetning"]
     [:th.mmm-tar.mmm-table-auto "Mengde (g)"]]]
   [:tbody
    [:tr
     [:th "Fettsyrer, total"]
     [:td.mmm-tar "27,0"]]
    [:tr
     [:th "Mettet fett, total"]
     [:td.mmm-tar "17,1"]]
    [:tr
     [:th "Enumettet fett, total"]
     [:td.mmm-tar "6,5"]]
    [:tr
     [:th "Flerumettet fett, total"]
     [:td.mmm-tar "0,6"]]
    [:tr
     [:th "Transfett"]
     [:td.mmm-tar "0,8"]]
    [:tr
     [:th "Kolestrol"]
     [:td.mmm-tar "0,07"]]]])

(defscene table-without-thead-and-tbody
  "En standard tabell uten thead og tbody"
  [:table.mmm-table.mmm-table-zebra
   [:tr
    [:th "Fettsyrer, total"]
    [:td.mmm-tar "27,0"]]
   [:tr
    [:th "Mettet fett, total"]
    [:td.mmm-tar "17,1"]]
   [:tr
    [:th "Enumettet fett, total"]
    [:td.mmm-tar "6,5"]]
   [:tr
    [:th "Flerumettet fett, total"]
    [:td.mmm-tar "0,6"]]
   [:tr
    [:th "Transfett"]
    [:td.mmm-tar "0,8"]]
   [:tr
    [:th "Kolestrol"]
    [:td.mmm-tar "0,07"]]])

(defscene table-with-minimal-column
  "Ved å sette .mmm-td-min på en kolonne får den bare akkurat så mye plass som
  innholdet krever, med white-space: nowrap."
  [:table.mmm-table.mmm-table-zebra
   [:tr
    [:th "Fettsyrer, total"]
    [:td.mmm-tar.mmm-td-min "27,0"]]
   [:tr
    [:th "Mettet fett, total"]
    [:td.mmm-tar.mmm-td-min "17,1"]]
   [:tr
    [:th "Enumettet fett, total"]
    [:td.mmm-tar.mmm-td-min "6,5"]]
   [:tr
    [:th "Flerumettet fett, total"]
    [:td.mmm-tar.mmm-td-min "0,6"]]
   [:tr
    [:th "Transfett"]
    [:td.mmm-tar.mmm-td-min "0,8"]]
   [:tr
    [:th "Kolestrol"]
    [:td.mmm-tar.mmm-td-min "0,07"]]])

(defscene table-with-inline-header
  "Ved å sette `.mmm-header` på en rad i `tbody` kan du få samme stil som
  headerne på tabellen. Kan være nyttig til lange tabeller med under-headinger."
  [:table.mmm-table.mmm-table-zebra
   [:tr
    [:th "Fettsyrer, total"]
    [:td.mmm-tar.mmm-td-min "27,0"]]
   [:tr
    [:th "Mettet fett, total"]
    [:td.mmm-tar.mmm-td-min "17,1"]]
   [:tr.mmm-thead
    [:th {:colspan 2} "Det andre fettet"]]
   [:tr
    [:th "Enumettet fett, total"]
    [:td.mmm-tar.mmm-td-min "6,5"]]
   [:tr
    [:th "Flerumettet fett, total"]
    [:td.mmm-tar.mmm-td-min "0,6"]]
   [:tr
    [:th "Transfett"]
    [:td.mmm-tar.mmm-td-min "0,8"]]
   [:tr
    [:th "Kolestrol"]
    [:td.mmm-tar.mmm-td-min "0,07"]]])

(defscene table-with-highlighted-row
  "Ved å sette `.mmm-highlight` på en rad får du satt ekstra fokus på innholdet
  i raden. Denne tabellen har også .mmm-table-hover som gir musefokus på rader."
  [:table.mmm-table.mmm-table-zebra.mmm-table-hover
   [:tr
    [:th "Fettsyrer, total"]
    [:td.mmm-tar.mmm-td-min "27,0"]]
   [:tr
    [:th "Mettet fett, total"]
    [:td.mmm-tar.mmm-td-min "17,1"]]
   [:tr.mmm-highlight
    [:th {:colspan 2} "Det andre fettet"]]
   [:tr
    [:th "Enumettet fett, total"]
    [:td.mmm-tar.mmm-td-min "6,5"]]
   [:tr
    [:th "Flerumettet fett, total"]
    [:td.mmm-tar.mmm-td-min "0,6"]]
   [:tr
    [:th "Transfett"]
    [:td.mmm-tar.mmm-td-min "0,8"]]
   [:tr
    [:th "Kolestrol"]
    [:td.mmm-tar.mmm-td-min "0,07"]]])
