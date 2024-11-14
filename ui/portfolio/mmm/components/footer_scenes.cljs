(ns mmm.components.footer-scenes
  (:require [mmm.components.footer :refer [Footer]]
            [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Footer"})

(defscene footer
  (e/block {:class "mmm-themed mmm-brand-theme4"}
   (Footer
    {:cols [{:title "Om Nettstedet"
             :items [{:text "Nyhetsbrev"
                      :url "#"}
                     {:text "Personvernerklæring og informasjonskapsler"
                      :url "#"}
                     {:text "Tilgjengelegheitserklæring (uustatus.no)"
                      :url "#"}
                     {:text "Åpne data (API)"
                      :url "#"}]}
            {:title "Om Mattilsynet"
             :items [{:text "Om oss"
                      :url "#"}
                     {:text "Ledige stillinger"
                      :url "#"}
                     {:text "Kontakt oss"
                      :url "#"}
                     {:text "Varsle oss"
                      :url "#"}]}]})))

(defscene compact-footer
  (e/block {:class "mmm-themed mmm-brand-theme4"}
   (Footer
    {:cols [{:title "Om nettstedet"
             :header-class "mmm-h6"
             :text (list "Matvaretabellen er en tjeneste fra Mattilsynet. For øvrige tjenester, se " [:a {:href "https://mattilsynet.no"} "mattilsynet.no"])}
            {:title "Om Mattilsynet"
             :header-class "mmm-h6"
             :items [{:url "https://www.mattilsynet.no/varsle"
                      :text "Varsle Mattilsynet"}
                     {:url "https://www.mattilsynet.no/om-mattilsynet/personvernerklaering-og-informasjonskapsler"
                      :text "Personvernerklæring"}]}
            {:title "Kontakt"
             :header-class "mmm-h6"
             :items [{:text "Ring oss på 22 40 00 00"}
                     {:url "https://www.mattilsynet.no/kontakt-oss"
                      :text "Kontakt oss"}]}]})))

(defscene compact-footer-new
  (e/block {:class "mmm-themed mmm-brand-theme4"}
           (Footer
            {:theme "mt2023"
             :cols [{:title "Om Mattilsynet"
                     :header-class "mmm-h6"
                     :items [{:url "https://www.mattilsynet.no/varsle"
                              :text "Varsle Mattilsynet"}
                             {:url "https://www.mattilsynet.no/om-mattilsynet/personvernerklaering-og-informasjonskapsler"
                              :text "Personvernerklæring"}]}
                    {:title "Kontakt"
                     :header-class "mmm-h6"
                     :items [{:text "Ring oss på 22 40 00 00"}
                             {:url "https://www.mattilsynet.no/kontakt-oss"
                              :text "Kontakt oss"}]}
                    {:title "Om nettstedet"
                     :header-class "mmm-h6"
                     :text (list "Matvaretabellen er en tjeneste fra Mattilsynet. For øvrige tjenester, se " [:a {:href "https://mattilsynet.no"} "mattilsynet.no"])}]})))
