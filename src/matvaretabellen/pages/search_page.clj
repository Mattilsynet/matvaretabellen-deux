(ns matvaretabellen.pages.search-page
  (:require [mattilsynet.design :as mtds]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.ui.client-table :as client-table]
            [matvaretabellen.urls :as urls]
            [mmm.components.search-input :refer [SearchInput]]
            [phosphor.icons :as icons]))

(defn render-excel-download-button [locale]
  [:a {:class (mtds/classes :button)
       :data-variant "secondary"
       :href (urls/get-foods-excel-url locale)}
   (icons/render :phosphor.regular/arrow-down)
   [:i18n ::download-everything]])

(defn render [context page]
  (layout/layout
   context
   page
   [:head
    [:title [:i18n ::page-title]]
    [:meta
     {:property "og:description"
      :content [:i18n ::open-graph-description]}]]
   [:body {:data-size "lg"}
    [:div {:class (mtds/classes :grid) :data-gap "12"}
     (layout/render-header
      {:locale (:page/locale page)
       :app/config (:app/config context)}
      urls/get-search-url)
     [:form {:class (mtds/classes :grid)
             :data-center "sm"
             :action (urls/get-search-url (:page/locale page))
             :method :get}
      [:h1 {:class (mtds/classes :heading) :data-size "md"} [:i18n :i18n/search-label]]
      (SearchInput
       {:button {:text [:i18n :i18n/search-button]}
        :input {:name "q"}
        :class :mvt-filter-search})
      [:div {:class (mtds/classes :flex) :data-size "sm"}
       (client-table/render-food-groups-toggle)
       (client-table/render-nutrients-toggle)]]
     [:div {:class (mtds/classes :flex)
            :data-justify "space-between"
            :data-size "md"
            :data-center "xl"}
      [:div {:class (mtds/classes :flex) :data-align "center"}
       (client-table/render-download-csv-button)
       [:p.mvt-clear-downloads.mmm-hidden
        [:a {:class (mtds/classes :button)}
         (icons/render :phosphor.regular/x)
         [:i18n ::clear-download]]]]
      (render-excel-download-button (:page/locale page))]
     [:div {:class (mtds/classes :grid) :data-center "xl"}
      (client-table/render-column-settings (:foods/db context))
      (client-table/render-food-group-settings context page)
      (client-table/render-table-skeleton (:foods/db context))]]]))
