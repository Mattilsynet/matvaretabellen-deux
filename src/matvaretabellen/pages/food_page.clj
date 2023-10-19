(ns matvaretabellen.pages.food-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.components.toc :refer [Toc]]
            [matvaretabellen.crumbs :as crumbs]
            [mt-designsystem.components.breadcrumbs :refer [Breadcrumbs]]
            [mt-designsystem.components.site-header :refer [SiteHeader]]))

(defn render [context _db page]
  (let [food (d/entity (:foods/db context) [:food/id (:food/id page)])
        locale (:page/locale page)
        food-name (get-in food [:food/name locale])]
    [:html
     [:body
      (SiteHeader {:home-url "/"})
      [:div
       [:div.mvt-hero-banner
        [:div.container
         (Breadcrumbs
          {:links (crumbs/crumble locale
                                  (:food/food-group food)
                                  {:text food-name})})]]
       [:div.mvt-hero-banner
        [:div.container
         [:div {:style {:display "flex"}}
          [:div {:style {:flex "1"}}
           [:h1.h1 food-name]
           [:div.intro.mtl
            [:div [:i18n :food/food-id {:id (:food/id food)}]]
            [:div [:i18n :food/category {:category (get-in food [:food/food-group :food-group/name locale])}] ]
            [:div [:i18n :food/latin-name {:food/latin-name (:food/latin-name food)}]]]]
          (Toc {:title [:i18n :food/toc-title]
                :contents [{:title [:i18n :food/nutrition-title]
                            :href "#naeringsinnhold"
                            :contents [{:title [:i18n :food/energy-title]
                                        :href "#energi"}
                                       {:title [:i18n :food/fat-title]
                                        :href "#fett"}
                                       {:title [:i18n :food/carbohydrates-title]
                                        :href "#karbohydrater"}
                                       {:title [:i18n :food/vitamins-title]
                                        :href "#vitaminer"}
                                       {:title [:i18n :food/minerals-title]
                                        :href "#mineraler"}]}
                           {:title [:i18n :food/adi-title]
                            :href "#adi"}
                           {:title [:i18n :food/description-title]
                            :href "#beskrivelse"}]})]]]]]]))
