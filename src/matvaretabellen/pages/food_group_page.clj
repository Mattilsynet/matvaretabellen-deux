(ns matvaretabellen.pages.food-group-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.footer :refer [CompactSiteFooter]]
            [mmm.components.site-header :refer [SiteHeader]]))

(defn render [context _db page]
  (let [food-group (d/entity (:foods/db context)
                             [:food-group/id (:page/food-group-id page)])
        details (d/entity (:app/db context)
                          [:food-group/id (:page/food-group-id page)])
        locale (:page/locale page)]
    [:html {:class "mmm"}
     [:body
      (SiteHeader {:home-url "/"})
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
            {:count (count (:food/_food-group food-group))}]]
          [:div.mmm-text.mmm-preamble
           [:p (get-in details [:food-group/short-description locale])]]]
         [:aside.mmm-desktop {:style {:flex-basis "40%"}}
          [:img {:src (:food-group/illustration details)
                 :width 300}]]]]]
      [:div.mmm-container.mmm-section
       (CompactSiteFooter)]]]))
