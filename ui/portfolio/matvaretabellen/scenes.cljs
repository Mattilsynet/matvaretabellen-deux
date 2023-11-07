(ns matvaretabellen.scenes
  (:require [matvaretabellen.components.legend-scenes]
            [matvaretabellen.components.nutrition-table-scenes]
            [matvaretabellen.components.pie-chart-scenes]
            [mmm.scenes]
            [portfolio.data :as data]
            [portfolio.ui :as ui]))

:matvaretabellen.components.legend-scenes/keep
:matvaretabellen.components.nutrition-table-scenes/keep
:matvaretabellen.components.pie-chart-scenes/keep
:mmm.scenes/keep

(data/register-collection!
 :matvaretabellen.components
 {:title "Komponenter (Matvaretabellen)"})

(data/register-collection!
 :mmm.components
 {:title "Komponenter (MMM)"})

(data/register-collection!
 :mmm.elements
 {:title "Elementer"})

(data/register-collection!
 :mmm.layouts
 {:title "Layouts"})

(defonce app
  (ui/start!
   {:config
    {:canvas-path "canvas.html"
     :css-paths ["/css/theme-mt90s.css"
                 "/css/mmm.css"
                 "/css/matvaretabellen.css"]}}))
