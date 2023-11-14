(ns matvaretabellen.pages.food-group-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.components.comparison :as comparison]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.food :as food]
            [matvaretabellen.pages.food-page :as food-page]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.button :refer [Button]]
            [mmm.components.footer :refer [CompactSiteFooter]]
            [mmm.components.site-header :refer [SiteHeader]]))

(defn prepare-foods-table [locale foods]
  {:headers [{:text "Matvare"} {}]
   :rows (for [food foods]
           [{:text [:a.mmm-link {:href (urls/get-food-url locale food)}
                    [:i18n :i18n/lookup (:food/name food)]]}
            (comparison/render-toggle-cell food locale)])})

(defn render [context _db page]
  (let [food-group (d/entity (:foods/db context)
                             [:food-group/id (:page/food-group-id page)])
        details (d/entity (:app/db context)
                          [:food-group/id (:page/food-group-id page)])
        foods (food/get-all-food-group-foods food-group)
        locale (:page/locale page)]
    [:html {:class "mmm"}
     [:body
      (SiteHeader {:home-url "/"
                   :extra-link {:text [:i18n :i18n/other-language]
                                :url (urls/get-food-group-url
                                      ({:en :nb :nb :en} locale)
                                      food-group)}})
      [:div.mmm-themed.mmm-brand-theme1
       [:div.mmm-container.mmm-section
        (Breadcrumbs
         {:links (crumbs/crumble locale
                                 {:text [:i18n ::crumbs/search-label]
                                  :url (urls/get-base-url locale)}
                                 food-group)})]
       [:div.mmm-container.mmm-section.mmm-mvxl
        [:div.mmm-media
         [:article.mmm-vert-layout-m
          [:div [:h1.mmm-h1 (get-in food-group [:food-group/name locale])]
           [:i18n :i18n/number-of-foods
            {:count (count foods)}]]
          [:div.mmm-text.mmm-preamble
           [:p (or (get-in details [:food-group/long-description locale])
                   (get-in details [:food-group/short-description locale]))]]
          [:div
           (Button {:text [:i18n ::download-these]
                    :href (urls/get-food-group-excel-url locale food-group)
                    :icon :fontawesome.solid/arrow-down
                    :inline? true
                    :secondary? true})]]
         [:aside.mmm-desktop {:style {:flex-basis "40%"}}
          [:img {:src (:food-group/illustration details)
                 :width 300}]]]]]

      [:div.mmm-container-medium.mmm-section.mmm-vert-layout-m.mmm-mobile-phn
       (->> (prepare-foods-table locale foods)
            food-page/render-table)]

      (comparison/render-comparison-drawer locale)

      [:div.mmm-container.mmm-section
       (CompactSiteFooter)]]]))
