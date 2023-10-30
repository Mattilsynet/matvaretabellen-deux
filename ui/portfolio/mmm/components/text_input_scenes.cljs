(ns mmm.components.text-input-scenes
  (:require [mmm.components.text-input :refer [TextInput]]
            [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene text-input
  (e/block (TextInput {})))
