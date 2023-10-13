(ns matvaretabellen.components.toc-scenes
  (:require [matvaretabellen.components.toc :refer [Toc]]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene basic-toc
  (Toc {:title "Innhold"
        :contents [{:title "Næringsinnhold"
                    :href "#naeringsinnhold"
                    :contents [{:title "Sammensetning og energiinnhold"
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
