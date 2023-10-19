(ns matvaretabellen.pages.frontpage
  (:require [matvaretabellen.crumbs :as crumbs]
            [mt-designsystem.components.breadcrumbs :refer [Breadcrumbs]]
            [mt-designsystem.components.search-input :refer [SearchInput]]
            [mt-designsystem.components.site-header :refer [SiteHeader]]))

(defn render [_context _db page]
  [:html
   [:body
    (SiteHeader {:home-url "/"})
    [:div
     [:div.container.mtl
      (Breadcrumbs
       {:links (crumbs/crumble (:page/locale page))})]
     [:div.container.mtl
      [:div.search-input-wrap
       (SearchInput {:label [:i18n ::search-label]
                     :button {:text [:i18n ::search-button]}
                     :input {:name "foods-search"}
                     :autocomplete-id "foods-results"})]]]]])
