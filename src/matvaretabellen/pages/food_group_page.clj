(ns matvaretabellen.pages.food-group-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.footer :refer [CompactSiteFooter]]
            [mmm.components.site-header :refer [SiteHeader]]))

(defn render [context _db page]
  (let [food-group (d/entity (:foods/db context)
                             [:food-group/id (:food-group/id page)])
        locale (:page/locale page)]
    [:html {:class "mmm"}
     [:body
      (SiteHeader {:home-url "/"})
      [:div.mmm-themed.mmm-brand-theme1
       [:div.mmm-container.mmm-section
        (Breadcrumbs {:links (crumbs/crumble locale food-group)})]
       [:div.mmm-container.mmm-section.mmm-mtxl.mmm-vert-layout-m
        [:h1.mmm-h1 (get-in food-group [:food-group/name locale])]
        [:div.mmm-cards
         (for [child (:food-group/_parent food-group)]
           (let [the-name (get-in child [:food-group/name locale])]
             [:a.mmm-card.mmm-link {:href (urls/get-food-group-url locale the-name)}
              the-name]))]]]
      [:div.mmm-container.mmm-section
       (CompactSiteFooter)]]]))
