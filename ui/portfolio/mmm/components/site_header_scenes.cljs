(ns mmm.components.site-header-scenes
  (:require [mmm.components.site-header :refer [SiteHeader]]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Header"})

(defscene basic-header
  :canvas/layout {:kind :rows
                  :xs [{:viewport/padding [0 0 0 0]}]}
  (SiteHeader {:home-url "#"}))

(defscene header-with-link
  :canvas/layout {:kind :rows
                  :xs [{:viewport/padding [0 0 0 0]}]}
  (SiteHeader {:home-url "#"
               :extra-link {:text "In English"
                            :url "#"}}))
