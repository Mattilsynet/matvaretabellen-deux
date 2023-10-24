(ns matvaretabellen.scenes
  (:require [matvaretabellen.colors.mattilsynet-90s-scenes]
            [matvaretabellen.components.nutrition-table-scenes]
            [matvaretabellen.components.pie-chart-scenes]
            [matvaretabellen.components.toc-scenes]
            [matvaretabellen.elements.image-scenes]
            [matvaretabellen.elements.typography-scenes]
            [matvaretabellen.layouts.container-scenes]
            [mt-designsystem.scenes]
            [portfolio.ui :as ui]))

:matvaretabellen.colors.mattilsynet-90s-scenes/keep
:matvaretabellen.components.nutrition-table-scenes/keep
:matvaretabellen.components.pie-chart-scenes/keep
:matvaretabellen.components.toc-scenes/keep
:matvaretabellen.elements.image-scenes/keep
:matvaretabellen.elements.typography-scenes/keep
:matvaretabellen.layouts.container-scenes/keep
:mt-designsystem.scenes/keep

(defonce app
  (ui/start!
   {:config
    {:canvas-path "canvas.html"
     :css-paths ["/css/theme-mt90s.css"
                 "/css/mvt.css"
                 "/css/mt-designsystem.css"
                 "/css/matvaretabellen.css"]
     :viewport/defaults
     {:viewport/padding [0 0 24 24]}}}))
