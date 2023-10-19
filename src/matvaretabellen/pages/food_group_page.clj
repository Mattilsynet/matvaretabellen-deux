(ns matvaretabellen.pages.food-group-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.urls :as urls]
            [mt-designsystem.components.breadcrumbs :refer [Breadcrumbs]]
            [mt-designsystem.components.site-header :refer [SiteHeader]]))

(defn render [context _db page]
  (let [food-group (d/entity (:foods/db context)
                             [:food-group/id (:food-group/id page)])
        locale (:page/locale page)]
    [:html
     [:body
      (SiteHeader {:home-url "/"})
      [:div
       [:div.mvt-hero-banner
        [:div.container
         (Breadcrumbs {:links (crumbs/crumble locale food-group)})]]
       [:div.mvt-hero-banner
        [:div.container
         [:h1.h1 (get-in food-group [:food-group/name locale])]
         [:div.mvt-cards.mtl
          (for [child (:food-group/_parent food-group)]
            (let [the-name (get-in child [:food-group/name locale])]
              [:a.mvt-card {:href (urls/get-food-group-url locale the-name)}
               the-name]))]]]]]]))
