(ns mt-designsystem.components.search-input-scenes
  (:require [mt-designsystem.components.search-input :refer [SearchInput]]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene empty-search
  (SearchInput {:label "Søk i Matvaretabellen"
                :button {:text "Søk"}
                :input {:name "my-search"}}))
