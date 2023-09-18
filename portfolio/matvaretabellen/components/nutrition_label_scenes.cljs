(ns matvaretabellen.components.nutrition-label-scenes
  (:require [matvaretabellen.components.nutrition-label :refer [NutritionLabel]]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene basic-label
  (NutritionLabel {:title "Næringsdeklarasjon"}))
