(ns matvaretabellen.pages.food-groups-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.footer :refer [SiteFooter]]
            [mmm.components.site-header :refer [SiteHeader]]))

(defn render [context _db page]
  (let [db (:foods/db context)
        food-groups (map #(d/entity db %)
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
                                 {:text [:i18n ::crumbs/all-food-groups]})})]
       [:div.mmm-container.mmm-section.mmm-mtxl
        [:h1.mmm-h1 [:i18n ::all-food-groups]]
        [:div.mmm-cards.mmm-block
         (for [child food-groups]
           (let [the-name (get-in child [:food-group/name locale])]
             [:a.mmm-card.mmm-link {:href (urls/get-food-group-url locale the-name)}
              the-name]))]]]
      [:div.mmm-container.mmm-section
       (SiteFooter)]]]))
