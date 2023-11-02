(ns mmm.components.footer-scenes
  (:require [mmm.components.footer :refer [Footer]]
            [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Footer"})

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
