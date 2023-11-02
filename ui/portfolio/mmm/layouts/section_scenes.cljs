(ns mmm.layouts.section-scenes
  (:require [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Seksjoner"})

(defscene section
  ".mmm-section gir vertikalt oppdelte deler av siden passe med vertikal luft"
  [:div
   [:div.mmm-section
    [:div {:style {:overflow "hidden"
                   :padding "0 20px"}}
     (e/h2 "Jeg er i en seksjon")
     (e/p "Og jeg med. Sammen er vi en liten søt enhet.")]]
   [:div.mmm-section {:style {:background "var(--mt-color-green-100)"}}
    [:div {:style {:overflow "hidden"
                   :padding "0 20px"}}
     (e/h2 "Jeg er i neste seksjon")
     (e/p "Bakgrunnsfargen hjelper litt med å vise frem luften mellom seksjonene.")]]
   [:div.mmm-section
    [:div {:style {:overflow "hidden"
                   :padding "0 20px"}}
     (e/h2 "Jeg er i siste seksjon")
     (e/p "Under ser du en hund som koser seg godt")
     (e/p (e/img {:src "/images/dog.jpg"}))]]])
