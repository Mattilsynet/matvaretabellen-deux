(ns matvaretabellen.elements.table-scenes
  (:require [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene table
  "En standard tabell med .mvt-table"
  [:div.mvt-mod
   [:table.mvt-table
    [:caption "Fettsyrer"]
    [:thead
     [:tr
      [:th "Fettsyrer, sammensetning"]
      [:th.mvt-tar.mvt-table-auto "Mengde (g)"]]]
    [:tbody
     [:tr
      [:th "Fettsyrer, total"]
      [:td.mvt-tar "27,0"]]
     [:tr
      [:th "Mettet fett, total"]
      [:td.mvt-tar "17,1"]]
     [:tr
      [:th "Enumettet fett, total"]
      [:td.mvt-tar "6,5"]]
     [:tr
      [:th "Flerumettet fett, total"]
      [:td.mvt-tar "0,6"]]
     [:tr
      [:th "Transfett"]
      [:td.mvt-tar "0,8"]]
     [:tr
      [:th "Kolestrol"]
      [:td.mvt-tar "0,07"]]]]])

(defscene zebra-striped-table
  "Ved å legge .mvt-table-zebra på en standard tabell blir like stripete som en
  av savannens flotteste equidae"
  [:div.mvt-mod
   [:table.mvt-table.mvt-table-zebra
    [:caption "Fettsyrer"]
    [:thead
     [:tr
      [:th "Fettsyrer, sammensetning"]
      [:th.mvt-tar.mvt-table-auto "Mengde (g)"]]]
    [:tbody
     [:tr
      [:th "Fettsyrer, total"]
      [:td.mvt-tar "27,0"]]
     [:tr
      [:th "Mettet fett, total"]
      [:td.mvt-tar "17,1"]]
     [:tr
      [:th "Enumettet fett, total"]
      [:td.mvt-tar "6,5"]]
     [:tr
      [:th "Flerumettet fett, total"]
      [:td.mvt-tar "0,6"]]
     [:tr
      [:th "Transfett"]
      [:td.mvt-tar "0,8"]]
     [:tr
      [:th "Kolestrol"]
      [:td.mvt-tar "0,07"]]]]])

(defscene table-without-thead-and-tbody
  "En standard tabell uten thead og tbody"
  [:div.mvt-mod
   [:table.mvt-table.mvt-table-zebra
    [:tr
     [:th "Fettsyrer, total"]
     [:td.mvt-tar "27,0"]]
    [:tr
     [:th "Mettet fett, total"]
     [:td.mvt-tar "17,1"]]
    [:tr
     [:th "Enumettet fett, total"]
     [:td.mvt-tar "6,5"]]
    [:tr
     [:th "Flerumettet fett, total"]
     [:td.mvt-tar "0,6"]]
    [:tr
     [:th "Transfett"]
     [:td.mvt-tar "0,8"]]
    [:tr
     [:th "Kolestrol"]
     [:td.mvt-tar "0,07"]]]])
