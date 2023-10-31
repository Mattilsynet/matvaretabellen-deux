(ns matvaretabellen.scenes
  (:require [matvaretabellen.components.nutrition-table-scenes]
            [matvaretabellen.components.pie-chart-scenes]
            [matvaretabellen.components.toc-scenes]
            [mt-designsystem.scenes]
            [portfolio.ui :as ui]))

:matvaretabellen.components.nutrition-table-scenes/keep
:matvaretabellen.components.pie-chart-scenes/keep
:matvaretabellen.components.toc-scenes/keep
:mt-designsystem.scenes/keep

(defonce app
  (ui/start!
   {:config
    {:css-paths ["/css/mt-designsystem.css"
                 "/css/matvaretabellen.css"]}}))
