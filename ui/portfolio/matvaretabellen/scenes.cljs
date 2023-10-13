(ns matvaretabellen.scenes
  (:require [matvaretabellen.components.nutrition-table-scenes]
            [matvaretabellen.components.toc-scenes]
            [mt-designsystem.scenes]
            [portfolio.ui :as ui]))

:matvaretabellen.components.nutrition-table-scenes/keep
:matvaretabellen.components.toc-scenes/keep
:mt-designsystem.scenes/keep

(def app
  (ui/start!
   {:config
    {:css-paths ["/css/mt-designsystem.css"
                 "/css/matvaretabellen.css"]}}))
