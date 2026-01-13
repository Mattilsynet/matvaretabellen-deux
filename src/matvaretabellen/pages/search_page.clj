(ns matvaretabellen.pages.search-page
  (:require [mattilsynet.design :as m]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.ui.client-table :as client-table]
            [matvaretabellen.ui.search-input :refer [SearchInput]]
            [matvaretabellen.urls :as urls]
            [phosphor.icons :as icons]))

(defn render-excel-download-button [locale]
  [:a {:class (m/c :button)
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
    [:div {:class (m/c :grid) :data-gap "12"}
     (layout/render-header
      {:locale (:page/locale page)
       :app/config (:app/config context)}
      urls/get-search-url)
     [:form {:class (m/c :grid)
             :data-center "sm"
             :action (urls/get-search-url (:page/locale page))
             :method :get}
      [:h1 {:class (m/c :heading) :data-size "md"} [:i18n :i18n/search-label]]
      (SearchInput
       {:button {:text [:i18n :i18n/search-button]}
        :input {:name "q" :autocomplete "off"}
        :class :mvt-filter-search})
      [:div {:class (m/c :flex) :data-size "sm"}
       (client-table/render-food-groups-toggle)
       (client-table/render-nutrients-toggle)]]
     [:div {:class (m/c :flex)
            :data-justify "space-between"
            :data-size "md"
            :data-center "xl"}
      [:div {:class (m/c :flex) :data-align "center"}
       (client-table/render-download-csv-button)
       [:p.mvt-clear-downloads
        {:hidden "true"}
        [:a {:class (m/c :button)}
         (icons/render :phosphor.regular/x)
         [:i18n ::clear-download]]]]
      (render-excel-download-button (:page/locale page))]
     [:div {:class (m/c :grid) :data-center "xl"}
      (client-table/render-column-settings (:foods/db context))
      (client-table/render-food-group-settings context page)
      (client-table/render-table-skeleton (:foods/db context))]]]))
