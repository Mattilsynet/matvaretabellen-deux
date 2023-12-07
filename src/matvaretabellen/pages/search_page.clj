(ns matvaretabellen.pages.search-page
  (:require [matvaretabellen.layout :as layout]
            [matvaretabellen.pages.table-page :as table-page]
            [matvaretabellen.urls :as urls]
            [mmm.components.search-input :refer [SearchInput]]))

(defn render [context page]
  (layout/layout
   context
   page
   [:head
    [:title [:i18n ::page-title]]
    [:meta
     {:property "og:description"
      :content [:i18n ::open-graph-description]}]]
   [:body
    (layout/render-header (:page/locale page) urls/get-search-url)
    [:form.mmm-container-narrow.mmm-section.mmm-mbl.mmm-mtxl
     {:action (urls/get-search-url (:page/locale page))
      :method :get}
     [:h1.mmm-h2.mmm-mbm [:i18n :i18n/search-label]]
     (SearchInput
      {:button {:text [:i18n :i18n/search-button]}
       :input {:name "q"}
       :class :mvt-filter-search})
     [:div.mmm-mts
      (table-page/render-food-groups-toggle)
      [:span.mmm-mlm (table-page/render-nutrients-toggle)]]]
    [:div.mmm-container.mmm-section.mmm-mobile-phn.mmm-vert-layout-m
     (table-page/render-column-settings (:foods/db context))
     [:div.mmm-cols.mmm-cols-d1_2
      (table-page/render-food-group-settings context page)
      (table-page/render-table-skeleton (:foods/db context))]]]))
