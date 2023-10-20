(ns matvaretabellen.pages.frontpage
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.components.toc :refer [Toc]]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.urls :as urls]
            [mt-designsystem.components.breadcrumbs :refer [Breadcrumbs]]
            [mt-designsystem.components.footer :refer [Footer]]
            [mt-designsystem.components.search-input :refer [SearchInput]]
            [mt-designsystem.components.site-header :refer [SiteHeader]]))

(defn get-food-url [locale db id]
  (urls/get-food-url locale (get-in (d/entity db [:food/id id]) [:food/name locale])))

(defn BananaTeaserBox [locale db]
  [:div.one-two-grid
   [:div.one-box
    [:div.flex.flex-center.flex-gap-m.phm
     [:img {:src "/images/banana.jpg"}]
     [:div
      [:h4 [:i18n ::did-you-know]]
      [:p [:i18n ::banana-nutrition-facts] " "
       [:a.nbr {:href (get-food-url locale db "06.525")}
        [:i18n ::read-more-about-banana]]]]]]
   [:a.two-box.pam {:href [:i18n ::crumbs/food-groups-url]}
    [:div
     [:p [:i18n ::all-food-groups]]
     [:p [:i18n ::see-all-food-groups-overview]]]]
   [:div.two-box.pam
    [:div
     [:p [:i18n ::all-nutrients]]
     [:p [:i18n ::see-all-nutrients-overview]]]]])

(defn render [_context db page]
  (let [locale (:page/locale page)]
    [:html
     [:body
      (SiteHeader {:home-url "/"})
      [:div
       [:div.container.mtl
        (Breadcrumbs
         {:links (crumbs/crumble locale)})]
       [:div.container.mtl
        [:div.search-input-wrap
         [:h1.h2.mbl [:i18n ::search-label]]
         (SearchInput {:button {:text [:i18n ::search-button]}
                       :input {:name "foods-search"}
                       :autocomplete-id "foods-results"})]]
       [:div.mvt-secondary-banner.mtxxl
        [:div.container
         (BananaTeaserBox locale db)]]
       [:div.container.mtl
        [:div.flex.flex-gap-l
         (Toc {:title [:i18n ::common-food-searches]
               :contents [{:title "Brød"
                           :href "/"}
                          {:title "Leverpostei"
                           :href "/"}
                          {:title "Lettmelk"
                           :href "/"}
                          {:title "Torsk"
                           :href "/"}]})
         [:div {:style {:flex 1}}
          (Toc {:title (list [:i18n ::new-in-food-table] " 2023")
                :contents [{:title "Egyptisk tahini"
                            :href "/"}
                           {:title "Gyroskjøtt"
                            :href "/"}
                           {:title "Bratwurst pølser"
                            :href "/"}
                           {:title "Vitamin K"
                            :href "/"}]})]
         (Toc {:title [:i18n ::seasonal-goods]
               :contents [{:title "Agurk"
                           :href "/"}
                          {:title "Arielle poteter"
                           :href "/"}
                          {:title "Blomkål"
                           :href "/"}
                          {:title "Rosenkål"
                           :href "/"}]})]]
       [:div.container.mtl
        (Footer)]]]]))
