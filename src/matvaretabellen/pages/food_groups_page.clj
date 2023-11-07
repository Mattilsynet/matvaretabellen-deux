(ns matvaretabellen.pages.food-groups-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.footer :refer [CompactSiteFooter]]
            [mmm.components.site-header :refer [SiteHeader]]))

(defn render [context db page]
  (let [food-groups (map #(d/entity db %)
                         (d/q '[:find [?e ...]
                                :where
                                [?e :food-group/id]
                                (not [?e :food-group/parent])]
                              db))
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
                                 {:text [:i18n ::crumbs/all-food-groups]})})]
       [:div.mmm-container.mmm-section.mmm-mtxl.mmm-vert-layout-m
        [:h1.mmm-h1 [:i18n ::all-food-groups]]
        [:div.mmm-cards
         (for [food-group food-groups]
           (let [the-name (get-in food-group [:food-group/name locale])
                 details (d/entity (:app/db context) [:food-group/id (:food-group/id food-group)])]
             [:a.mmm-card.mmm-link {:href (urls/get-food-group-url locale the-name)}
              [:div.mmm-media
               [:aside [:img.mmm-img {:src (:food-group/photo details)}]]
               [:article.mmm-text
                [:h3 the-name]
                [:p (get-in details [:food-group/short-description locale])]]]]))]]]
      [:div.mmm-container.mmm-section
       (CompactSiteFooter)]]]))
