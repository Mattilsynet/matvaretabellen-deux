(ns mmm.elements.image-scenes
  (:require [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene image
  "Bilder har maksbredde på 100%"
  (e/block (e/img {:src "/images/dog.jpg"})))
