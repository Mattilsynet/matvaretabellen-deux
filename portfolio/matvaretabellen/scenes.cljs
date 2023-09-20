(ns matvaretabellen.scenes
  (:require [matvaretabellen.components.nutrition-table-scenes]
            [mt-designsystem.components.breadcrumbs-scenes]
            [mt-designsystem.components.search-input-scenes]
            [mt-designsystem.components.site-header-scenes]
            [portfolio.ui :as ui]))

:matvaretabellen.components.nutrition-table-scenes/keep
:mt-designsystem.components.breadcrumbs-scenes/keep
:mt-designsystem.components.search-input-scenes/keep
:mt-designsystem.components.site-header-scenes/keep

(def app
  (ui/start!
   {:config
    {:css-paths ["/css/mt-designsystem.css"
                 "/css/matvaretabellen.css"]}}))
