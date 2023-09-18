(ns matvaretabellen.scenes
  (:require [matvaretabellen.components.nutrition-label-scenes]
            [portfolio.ui :as ui]))

:matvaretabellen.components.nutrition-label-scenes/keep

(def app
  (ui/start!
   {:config
    {:css-paths ["/css/app.css"]}}))
