(ns matvaretabellen.components.search-input-scenes
  (:require [matvaretabellen.components.search-input :refer [SearchInput]]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene empty-search
  (SearchInput {:label "Søk i Matvaretabellen"
                :button {:text "Søk"}
                :input {:name "my-search"}}))

(defscene eple-search
  [:div {:style {:min-height "400px"}}
   (SearchInput {:label "Søk i Matvaretabellen"
                 :button {:text "Søk"}
                 :input {:name "my-search"}
                 :results [{:href "#" :text "Eple, tørket"}
                           {:href "#" :text "Eple, rått"}
                           {:href "#" :text "Eple, Granny Smith"}]})])
