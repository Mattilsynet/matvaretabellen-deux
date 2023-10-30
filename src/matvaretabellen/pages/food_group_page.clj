(ns matvaretabellen.pages.food-group-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.site-header :refer [SiteHeader]]))

(defn render [context _db page]
  (let [food-group (d/entity (:foods/db context)
                             [:food-group/id (:food-group/id page)])
        locale (:page/locale page)]
    [:html
     [:body
      (SiteHeader {:home-url "/"})
      [:div
       [:div.mmm-hero-banner
        [:div.container
         (Breadcrumbs {:links (crumbs/crumble locale food-group)})]]
       [:div.mmm-hero-banner
        [:div.container
         [:h1.h1 (get-in food-group [:food-group/name locale])]
         [:div.mmm-cards.mtl
          (for [child (:food-group/_parent food-group)]
            (let [the-name (get-in child [:food-group/name locale])]
              [:a.mmm-card {:href (urls/get-food-group-url locale the-name)}
               the-name]))]]]]]]))
