(ns mmm.layouts.vert-layout-scenes
  (:require [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Vertikal layout"})

(defscene vert-layout-m
  [:div.mmm-text.mmm-vert-layout-m
   [:h2 ".mmm-vert-layout-m"]
   [:p "Denne røveren sprer elementene sine vertikalt med en passende gap."]
   [:p "Mellomrommet skulle passe fint til feks blokkelementer med tekst."]])

(defscene vert-layout-s
  [:div.mmm-text.mmm-vert-layout-s
   [:h2 ".mmm-vert-layout-s"]
   [:p "Denne røveren sprer elementene sine vertikalt med en passende gap."]
   [:p "Mellomrommet er litt mer knepent og passer bra der elementene skal litt tettere på hverandre."]])

(defscene vert-layout-spread
  [:div.mmm-text.mmm-vert-layout-spread
   {:style {:height 400}}
   [:h2 ".mmm-vert-layout-spread"]
   [:p "Denne rakkeren sprer innholdet sitt vertikalt fra topp til bunn."]
   [:p "Denne layouten fungerer best når boksen av en eller annen grunn får mer
  vertikal plass enn strengt nødvendig (feks når den er sidestilt med en annen,
  større boks)"]])
