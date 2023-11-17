(ns mmm.components.toc-scenes
  (:require [fontawesome.icons :as icons]
            [mmm.components.toc :refer [Toc]]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Innholdsfortegnelse"})

(defscene basic-toc
  (Toc
   {:title "Innhold"
    :icon (icons/icon :fontawesome.solid/circle-info)
    :contents [{:title "Næringsinnhold"
                :href "#naeringsinnhold"
                :contents [{:title "Energigivende næringsstoffer"
                            :href "#energi"}
                           {:title "Fettsyrer"
                            :href "#fett"}
                           {:title "Karbohydrater"
                            :href "#karbohydrater"}
                           {:title "Vitaminer"
                            :href "#vitaminer"}
                           {:title "Mineraler"
                            :href "#mineraler"}]}
               {:title "Anbefalt daglig inntak (ADI)"
                :href "#adi"}
               {:title "Beskrivelse av matvaren"
                :href "#beskrivelse"}]}))
