(ns matvaretabellen.components.site-header-scenes
  (:require [matvaretabellen.components.site-header :refer [SiteHeader]]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene basic-header
  (SiteHeader {:home-url "#"}))
