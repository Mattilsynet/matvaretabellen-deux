(ns mmm.elements.table-scenes
  (:require [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene table
  "En standard tabell med .mmm-table"
  [:div.mmm-mod
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
      [:td.mmm-tar "0,07"]]]]])

(defscene zebra-striped-table
  "Ved å legge .mmm-table-zebra på en standard tabell blir like stripete som en
  av savannens flotteste equidae"
  [:div.mmm-mod
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
      [:td.mmm-tar "0,07"]]]]])

(defscene table-without-thead-and-tbody
  "En standard tabell uten thead og tbody"
  [:div.mmm-mod
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
     [:td.mmm-tar "0,07"]]]])
