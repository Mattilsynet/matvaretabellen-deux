(ns matvaretabellen.components.search-input-scenes
  (:require [matvaretabellen.components.search-input :refer [SearchInput]]
            [matvaretabellen.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene empty-search
  (e/block
   (SearchInput
    {:label "Søk i Matvaretabellen"
     :button {:text "Søk"}
     :input {:name "my-search"}})))

(defscene eple-search
  (e/block {:style {:min-height "200px"}}
   (SearchInput
    {:label "Søk i Matvaretabellen"
     :button {:text "Søk"}
     :input {:name "my-search" :value "epl"}
     :results [{:href "#" :text "Eple, tørket"}
               {:href "#" :text "Eple, rått"}
               {:href "#" :text "Eple, Granny Smith"}]})))

(defscene eple-search-navigating
  (e/block {:style {:min-height "200px"}}
   (SearchInput
    {:label "Søk i Matvaretabellen"
     :button {:text "Søk"}
     :input {:name "my-search" :value "epl"}
     :results [{:href "#" :text "Eple, tørket"}
               {:href "#" :text "Eple, rått" :selected? true}
               {:href "#" :text "Eple, Granny Smith"}]})))
