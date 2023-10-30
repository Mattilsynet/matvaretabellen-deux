(ns matvaretabellen.components.text-input-scenes
  (:require [matvaretabellen.components.text-input :refer [TextInput]]
            [matvaretabellen.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene text-input
  (e/block (TextInput {})))
