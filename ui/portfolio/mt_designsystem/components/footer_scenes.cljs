(ns mt-designsystem.components.footer-scenes
  (:require [matvaretabellen.elements :as e]
            [mt-designsystem.components.footer :refer [Footer]]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene footer
  (e/block
   (Footer
    {:cols [{:title "Om Nettstedet"
             :links [{:text "Nyhetsbrev"
                      :url "#"}
                     {:text "Personvernerklæring og informasjonskapsler"
                      :url "#"}
                     {:text "Tilgjengelegheitserklæring (uustatus.no)"
                      :url "#"}
                     {:text "Åpne data (API)"
                      :url "#"}]}
            {:title "Om Mattilsynet"
             :links [{:text "Om oss"
                      :url "#"}
                     {:text "Ledige stillinger"
                      :url "#"}
                     {:text "Kontakt oss"
                      :url "#"}
                     {:text "Varsle oss"
                      :url "#"}]}]})))
