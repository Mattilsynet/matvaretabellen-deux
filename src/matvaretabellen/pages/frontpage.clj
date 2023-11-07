(ns matvaretabellen.pages.frontpage
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.seeded-random :as rng]
            [matvaretabellen.urls :as urls]
            [mmm.components.footer :refer [CompactSiteFooter]]
            [mmm.components.search-input :refer [SearchInput]]
            [mmm.components.site-header :refer [SiteHeader]]
            [mmm.components.toc :refer [Toc]]))

(defn get-food-info [locale db id]
  (let [food-name (get-in (d/entity db [:food/id id]) [:food/name locale])]
    {:title food-name
     :href (urls/get-food-url locale food-name)}))

(defn BananaTeaserBox [locale db]
  [:div.mmm-banner-media-buttons.mmm-section.mmm-brand-theme2
   [:div.mmm-container
    [:div.mmm-media.mmm-media-stamp
     [:aside {:style {:width "30%"}}
      [:img.mmm-img {:src "/images/trivia/banana.jpg"}]]
     [:article.mmm-text.mmm-tight.mmm-vert-layout-s
      [:h4 [:i18n ::did-you-know]]
      [:p [:i18n ::banana-nutrition-facts]]
      [:p [:a.mmm-nbr (get-food-info locale db "06.525")
           [:i18n ::read-more-about-banana]]]]]
    [:div.mmm-buttons.mmm-text.mmm-tight
     [:a.mmm-banner-button.mmm-vert-layout-s
      {:href [:i18n ::crumbs/food-groups-url]}
      [:p [:strong [:i18n ::all-food-groups]]]
      [:p [:i18n ::see-all-food-groups-overview]]]
     [:a.mmm-banner-button.mmm-vert-layout-s
      {:href "/"}
      [:p [:strong [:i18n ::all-nutrients]]]
      [:p [:i18n ::see-all-nutrients-overview]]]]]])

(defn render [context db page]
  (let [locale (:page/locale page)]
    [:html {:class "mmm"}
     [:body
      (SiteHeader {:home-url "/"})
      [:form.mmm-container-narrow.mmm-section.mmm-mvxxl
       [:h1.mmm-h2.mmm-mbl [:i18n ::search-label]]
       (SearchInput {:button {:text [:i18n ::search-button]}
                     :input {:name "foods-search"}
                     :autocomplete-id "foods-results"})]
      (BananaTeaserBox locale db)
      [:div.mmm-container.mmm-section.mmm-cols
       (Toc {:title [:i18n ::common-food-searches]
             :contents [{:title "Egg" :href "?search=egg"}
                        {:title "Gulrot" :href "?search=gulrot"}
                        {:title "Havregryn" :href "?search=havregryn"}
                        {:title "Potet" :href "?search=potet"}]
             :class :mmm-col})
       (let [new-foods (:new-foods (:page/details page))]
         (Toc {:title (list [:i18n ::new-in-food-table] " " (:year new-foods))
               :contents (for [id (take 4 (rng/shuffle*
                                           (/ (.getEpochSecond (:time/instant context)) 5)
                                           (:food-ids new-foods)))]
                           (get-food-info locale db id))
               :class :mmm-col}))
       (Toc {:title [:i18n ::seasonal-goods]
             :contents (for [id ["06.010" "06.003" "06.016" "06.055"]]
                         (get-food-info locale db id))
             :class :mmm-col})]
      [:div.mmm-container.mmm-section
       (CompactSiteFooter)]]]))
