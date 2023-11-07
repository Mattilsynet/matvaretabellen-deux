(ns matvaretabellen.components.legend-scenes
  (:require [matvaretabellen.components.legend :refer [Legend]]
            [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene legendary-legend
  (e/block
   [:div {:style {:width 300}}
    (Legend {:entries [{:color "red" :label "Banan"}
                       {:color "yellow" :label "Jordbær"}
                       {:color "orange" :label "Blåbær"}
                       {:color "blue" :label "Appelsin"}]})]))
