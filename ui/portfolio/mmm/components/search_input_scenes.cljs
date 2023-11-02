(ns mmm.components.search-input-scenes
  (:require [fontawesome.icons :as icons]
            [mmm.components.search-input :refer [SearchInput]]
            [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Søkeinput"})

(defscene empty-search
  (e/block
   (SearchInput
    {:label "Søk i Matvaretabellen"
     :button {:text "Søk"
              :icon (icons/icon :fontawesome.solid/magnifying-glass)}
     :input {:name "my-search"}})))

(defscene eple-search
  (e/block {:style {:min-height "200px"}}
   (SearchInput
    {:label "Søk i Matvaretabellen"
     :button {:text "Søk"
              :icon (icons/icon :fontawesome.solid/magnifying-glass)}
     :input {:name "my-search" :value "epl"}
     :results [{:href "#" :text "Eple, tørket"}
               {:href "#" :text "Eple, rått"}
               {:href "#" :text "Eple, Granny Smith"}]})))

(defscene eple-search-navigating
  (e/block {:style {:min-height "200px"}}
   (SearchInput
    {:label "Søk i Matvaretabellen"
     :button {:text "Søk"
              :icon (icons/icon :fontawesome.solid/magnifying-glass)}
     :input {:name "my-search" :value "epl"}
     :results [{:href "#" :text "Eple, tørket"}
               {:href "#" :text "Eple, rått" :selected? true}
               {:href "#" :text "Eple, Granny Smith"}]})))
