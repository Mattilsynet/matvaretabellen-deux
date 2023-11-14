(ns mmm.components.text-input-scenes
  (:require [mmm.components.button :refer [Button]]
            [mmm.components.select :refer [Select]]
            [mmm.components.text-input :refer [TextInput]]
            [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Tekstfelt"})

(defscene text-input
  (e/block (TextInput {})))

(defscene form-inputs
  "Det er rimelig å forvente at skjema-elementene tar samme vertikale plass og
  ser ålreit ut sammen"
  [:div.mmm-flex.mmm-flex-gap
   (TextInput {})
   (Select {})
   (Button {:text "Snurr film!"})])
