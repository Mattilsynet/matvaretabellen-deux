(ns mt-designsystem.components.site-header-scenes
  (:require [mt-designsystem.components.site-header :refer [SiteHeader]]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene basic-header
  (SiteHeader {:home-url "#"}))
