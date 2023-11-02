(ns mmm.components.text-input-scenes
  (:require [mmm.components.text-input :refer [TextInput]]
            [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Tekstfelt"})

(defscene text-input
  (e/block (TextInput {})))
