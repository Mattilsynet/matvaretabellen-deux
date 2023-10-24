(ns matvaretabellen.scenes
  (:require [matvaretabellen.colors.mattilsynet-90s-scenes]
            [matvaretabellen.components.nutrition-table-scenes]
            [matvaretabellen.components.pie-chart-scenes]
            [matvaretabellen.components.toc-scenes]
            [mt-designsystem.scenes]
            [portfolio.ui :as ui]))

:matvaretabellen.colors.mattilsynet-90s-scenes/keep
:matvaretabellen.components.nutrition-table-scenes/keep
:matvaretabellen.components.pie-chart-scenes/keep
:matvaretabellen.components.toc-scenes/keep
:mt-designsystem.scenes/keep

(defonce app
  (ui/start!
   {:config
    {:css-paths ["/css/theme-mt90s.css"
                 "/css/mt-designsystem.css"
                 "/css/matvaretabellen.css"]}}))
