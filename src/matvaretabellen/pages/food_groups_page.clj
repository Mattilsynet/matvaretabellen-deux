(ns matvaretabellen.pages.food-groups-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
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
    [:html
     [:body
      (SiteHeader {:home-url "/"})
      [:div
       [:div.mmm-hero-banner
        [:div.container
         (Breadcrumbs
          {:links (crumbs/crumble locale
                                  {:text [:i18n ::crumbs/all-food-groups]})})]]
       [:div.mmm-hero-banner
        [:div.container
         [:h1.h1 [:i18n ::all-food-groups]]
         [:div.mmm-cards.mtl
          (for [child food-groups]
            (let [the-name (get-in child [:food-group/name locale])]
              [:a.mmm-card {:href (urls/get-food-group-url locale the-name)}
               the-name]))]]]]]]))
