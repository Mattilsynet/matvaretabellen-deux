(ns matvaretabellen.pages.food-group-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.urls :as urls]
            [mt-designsystem.components.breadcrumbs :refer [Breadcrumbs]]
            [mt-designsystem.components.site-header :refer [SiteHeader]]))

(defn create-food-group-breadcrumbs [locale food-group url?]
  (let [food-group-name (get-in food-group [:food-group/name locale])]
    (concat (when-let [parent (:food-group/parent food-group)]
              (create-food-group-breadcrumbs locale parent true))
            [(cond-> {:text food-group-name}
               url? (assoc :url (urls/get-food-group-url locale food-group-name)))])))

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
         (Breadcrumbs {:links (concat [{:text "Mattilsynet.no" :url "https://www.mattilsynet.no/"}
                                       {:text [:i18n :breadcrumbs/search-label] :url "/"}
                                       {:text [:i18n :breadcrumbs/all-food-groups] :url [:i18n :breadcrumbs/food-groups-url]}]
                                      (create-food-group-breadcrumbs locale food-group false))})]]
       [:div.mvt-hero-banner
        [:div.container
         [:h1.h1 (get-in food-group [:food-group/name locale])]
         [:div.mvt-cards.mtl
          (for [child (:food-group/_parent food-group)]
            (let [the-name (get-in child [:food-group/name locale])]
              [:a.mvt-card {:href (urls/get-food-group-url locale the-name)}
               the-name]))]]]]]]))
