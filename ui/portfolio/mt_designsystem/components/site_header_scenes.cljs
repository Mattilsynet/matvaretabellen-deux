(ns mt-designsystem.components.site-header-scenes
  (:require [mt-designsystem.components.site-header :refer [SiteHeader]]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene basic-header
  :canvas/layout {:kind :rows
                  :xs [{:viewport/padding [0 0 0 0]}]}
  (SiteHeader {:home-url "#"}))
