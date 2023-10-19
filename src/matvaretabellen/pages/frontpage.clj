(ns matvaretabellen.pages.frontpage
  (:require [mt-designsystem.components.breadcrumbs :refer [Breadcrumbs]]
            [mt-designsystem.components.search-input :refer [SearchInput]]
            [mt-designsystem.components.site-header :refer [SiteHeader]]))

(defn render [_context _db _page]
  [:html
   [:body
    (SiteHeader {:home-url "/"})
    [:div
     [:div.container.mtl
      (Breadcrumbs
       {:links [{:text "Mattilsynet.no" :url "https://www.mattilsynet.no/"}
                {:text [:i18n :frontpage/search-label]}]})]
     [:div.container.mtl
      [:div.search-input-wrap
       (SearchInput {:label [:i18n :frontpage/search-label]
                     :button {:text [:i18n :frontpage/search-button]}
                     :input {:name "foods-search"}
                     :autocomplete-id "foods-results"})]]]]])
