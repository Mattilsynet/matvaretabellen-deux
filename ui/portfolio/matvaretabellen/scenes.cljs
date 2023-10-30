(ns matvaretabellen.scenes
  (:require [matvaretabellen.colors.mattilsynet-90s-scenes]
            [matvaretabellen.components.button-scenes]
            [matvaretabellen.components.nutrition-table-scenes]
            [matvaretabellen.components.pie-chart-scenes]
            [matvaretabellen.components.search-input-scenes]
            [matvaretabellen.components.text-input-scenes]
            [matvaretabellen.components.toc-scenes]
            [matvaretabellen.elements.image-scenes]
            [matvaretabellen.elements.table-scenes]
            [matvaretabellen.elements.typography-scenes]
            [matvaretabellen.layouts.container-scenes]
            [mmm.scenes]
            [portfolio.data :as data]
            [portfolio.ui :as ui]))

:matvaretabellen.components.nutrition-table-scenes/keep
:matvaretabellen.components.pie-chart-scenes/keep
:matvaretabellen.components.search-input-scenes/keep
:matvaretabellen.components.text-input-scenes/keep
:matvaretabellen.components.toc-scenes/keep
:matvaretabellen.elements.image-scenes/keep
:matvaretabellen.elements.table-scenes/keep
:matvaretabellen.elements.typography-scenes/keep
:matvaretabellen.layouts.container-scenes/keep
:mt-designsystem.scenes/keep
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
