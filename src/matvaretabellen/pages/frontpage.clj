(ns matvaretabellen.pages.frontpage
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.footer :refer [SiteFooter]]
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
    [:div.mmm-media
     [:aside [:img.mmm-img {:src "/images/banana.jpg"}]]
     [:article.mmm-text.mmm-tight
      [:h4 [:i18n ::did-you-know]]
      [:p [:i18n ::banana-nutrition-facts]]
      [:p [:a.mmm-nbr (get-food-info locale db "06.525")
           [:i18n ::read-more-about-banana]]]]]
    [:div.mmm-buttons.mmm-text.mmm-tight
     [:a.mmm-banner-button {:href [:i18n ::crumbs/food-groups-url]}
      [:p [:i18n ::all-food-groups]]
      [:p [:i18n ::see-all-food-groups-overview]]]
     [:a.mmm-banner-button {:href "/"}
      [:p [:i18n ::all-nutrients]]
      [:p [:i18n ::see-all-nutrients-overview]]]]]])

(defn render [_context db page]
  (let [locale (:page/locale page)]
    [:html {:class "mmm"}
     [:body
      (SiteHeader {:home-url "/"})
      [:div.mmm-container.mmm-section
       (Breadcrumbs
        {:links (crumbs/crumble locale)})]
      [:form.mmm-container-narrow.mmm-section.mmm-mbxxl
       [:h1.mmm-h2.mmm-mbl [:i18n ::search-label]]
       (SearchInput {:button {:text [:i18n ::search-button]}
                     :input {:name "foods-search"}
                     :autocomplete-id "foods-results"})]
      (BananaTeaserBox locale db)
      [:div.mmm-container.mmm-section.mmm-threecol
       (Toc {:title [:i18n ::common-food-searches]
             :contents [{:title "Egg" :href "?search=egg"}
                        {:title "Gulrot" :href "?search=gulrot"}
                        {:title "Havregryn" :href "?search=havregryn"}
                        {:title "Potet" :href "?search=potet"}]
             :class :mmm-col})
       (Toc {:title (list [:i18n ::new-in-food-table] " 2023")
             :contents [{:title "Egyptisk tahini"
                         :href "/"}
                        {:title "Gyroskjøtt"
                         :href "/"}
                        {:title "Bratwurst pølser"
                         :href "/"}
                        {:title "Vitamin K"
                         :href "/"}]
             :class :mmm-col})
       (Toc {:title [:i18n ::seasonal-goods]
             :contents (for [id ["06.010" "06.003" "06.016" "06.055"]]
                         (get-food-info locale db id))
             :class :mmm-col})]
      [:div.mmm-container.mmm-section
       (SiteFooter)]]]))
