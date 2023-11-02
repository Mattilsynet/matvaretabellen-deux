(ns mmm.layouts.flex-scenes
  (:require [mmm.components.select :refer [Select]]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Flex"})

(defscene flex-container
  "Flex elementer med space-between og ankre innhold mot toppen"
  [:div.mmm-flex.mmm-block
   [:h2.mmm-h2.mmm-mbn "Næringsinnhold"]
   [:div
    [:p "Porsjonsstørrelse"]
    (Select
     {:id "portion-selector"
      :class "mmm-input-m"
      :options [[:option {:value "100"} "100 gram"]
                [:option {:value "1"} "1 porsjon"]]})]])

(defscene flex-container-bottom
  "Flex elementer med space-between og bruk .mmm-flex-bottom for å ankre
  innholdet mot bunnen"
  [:div.mmm-block.mmm-flex.mmm-flex-bottom
   [:h2.mmm-h2.mmm-mbn "Næringsinnhold"]
   [:div
    [:p "Porsjonsstørrelse"]
    (Select
     {:id "portion-selector"
      :class "mmm-input-m"
      :options [[:option {:value "100"} "100 gram"]
                [:option {:value "1"} "1 porsjon"]]})]])

(defscene flex-container-desktop
  "Som over, men kun på store nok skjermer"
  [:div.mmm-block.mmm-flex-desktop.mmm-flex-bottom
   [:h2.mmm-h2.mmm-mbn "Næringsinnhold"]
   [:div
    [:p "Porsjonsstørrelse"]
    (Select
     {:id "portion-selector"
      :class "mmm-input-m"
      :options [[:option {:value "100"} "100 gram"]
                [:option {:value "1"} "1 porsjon"]]})]])
