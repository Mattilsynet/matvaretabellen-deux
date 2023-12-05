(ns matvaretabellen.scenes
  (:require [matvaretabellen.components.cols-2-1-labeled-scenes]
            [matvaretabellen.components.legend-scenes]
            [matvaretabellen.components.nutrition-table-scenes]
            [matvaretabellen.components.pie-chart-scenes]
            [mmm.scenes]
            [portfolio.data :as data]
            [portfolio.ui :as ui]))

:matvaretabellen.components.cols-2-1-labeled-scenes/keep
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
                 "/css/theme-mt2023.css"
                 "/css/mmm.css"
                 "/css/matvaretabellen.css"]
     :background/options [{:id :mt90s-mode
                           :title "Mattilsynet 90s"
                           :value {:background/background-color "#fff"
                                   :background/body-class "mt90s"}}
                          {:id :mt2023-mode
                           :title "Mattilsynet 2023"
                           :value {:background/background-color "#fff"
                                   :background/body-class "mt2023"}}]}}))
