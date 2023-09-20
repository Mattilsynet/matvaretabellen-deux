(ns matvaretabellen.components.search-input-scenes
  (:require [matvaretabellen.components.search-input :refer [SearchInput]]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene empty-search
  (SearchInput {:label "Søk i Matvaretabellen"
                :button {:text "Søk"}
                :input {:name "my-search"}}))
