(ns matvaretabellen.scenes
  (:require [matvaretabellen.components.breadcrumbs-scenes]
            [matvaretabellen.components.nutrition-table-scenes]
            [portfolio.ui :as ui]))

:matvaretabellen.components.breadcrumbs-scenes/keep
:matvaretabellen.components.nutrition-table-scenes/keep

(def app
  (ui/start!
   {:config
    {:css-paths ["/css/app.css"
                 "/css/matvaretabellen.css"]}}))
