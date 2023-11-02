(ns mmm.layouts.passepartout-scenes
  (:require [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Passepartout"})

(defscene passepartout
  [:div.mmm-passepartout
   [:div.mmm-container-focused.mmm-vert-layout-m.mmm-text
    [:h2 ".mmm-passepartout"]
    [:p "Passepartout har brand-themet bakgrunn og litt padding. Kan
    kombineres med containere for å kontrollere bredde."]]])
