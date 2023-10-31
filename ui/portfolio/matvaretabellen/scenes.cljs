(ns matvaretabellen.scenes
  (:require [matvaretabellen.components.nutrition-table-scenes]
            [matvaretabellen.components.pie-chart-scenes]
            [mmm.scenes]
            [portfolio.data :as data]
            [portfolio.ui :as ui]))

:matvaretabellen.components.nutrition-table-scenes/keep
:matvaretabellen.components.pie-chart-scenes/keep
:mmm.scenes/keep

(data/register-collection!
 :matvaretabellen.components
 {:title "Matvaretabellen Components"})

(data/register-collection!
 :mmm.components
 {:title "MMM Components"})

(defonce app
  (ui/start!
   {:config
    {:canvas-path "canvas.html"
     :css-paths ["/css/theme-mt90s.css"
                 "/css/mmm.css"
                 "/css/matvaretabellen.css"]
     :viewport/defaults
     {:viewport/padding [0 0 24 24]}}}))
