(ns matvaretabellen.pages.food-groups-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.button :refer [Button]]
            [mmm.components.footer :refer [CompactSiteFooter]]
            [mmm.components.site-header :refer [SiteHeader]]))

(defn embellish-food-group [food-group app-db]
  (-> (into {} food-group)
      (into (d/entity app-db [:food-group/id (:food-group/id food-group)]))
      (update :food-group/category #(d/entity app-db %))))

(defn render [context food-db page]
  (let [app-db (:app/db context)
        food-groups (for [eid (d/q '[:find [?e ...]
                                     :where
                                     [?e :food-group/id]
                                     (not [?e :food-group/parent])]
                                   food-db)]
                      (embellish-food-group (d/entity food-db eid) app-db))
        locale (:page/locale page)]
    [:html {:class "mmm"}
     [:body
      (SiteHeader {:home-url "/"
                   :extra-link {:text [:i18n :i18n/other-language]
                                :url (urls/get-food-groups-url
                                      ({:en :nb :nb :en} locale))}})
      [:div.mmm-themed.mmm-brand-theme1
       [:div.mmm-container.mmm-section
        (Breadcrumbs
         {:links (crumbs/crumble locale
                                 {:text [:i18n ::crumbs/search-label]
                                  :url (urls/get-base-url locale)}
                                 {:text [:i18n ::crumbs/all-food-groups]})})]
       [:div.mmm-container.mmm-section.mmm-mvxl
        [:div.mmm-media
         [:article.mmm-vert-layout-m
          [:div [:h1.mmm-h1 [:i18n ::all-food-groups]]
           [:i18n :i18n/number-of-foods
            {:count (d/q '[:find (count ?e) .
                           :where [?e :food/id]] food-db)}]]
          [:div.mmm-text.mmm-preamble
           [:p [:i18n ::prose
                {:count (count food-groups)}]]]
          [:div
           (Button {:text [:i18n ::download-everything]
                    :href [:i18n ::download-url]
                    :icon :fontawesome.solid/arrow-down
                    :inline? true
                    :secondary? true})]]
         [:aside.mmm-desktop {:style {:flex-basis "40%"}}
          [:img {:src "/images/illustrations/alle-matvaregrupper.svg"
                 :width 300}]]]]]
      [:div.mmm-themed.mmm-brand-theme2
       (for [[category groups] (->> (group-by :food-group/category food-groups)
                                    (sort-by (comp :food-group-category/order first)))]
         [:div.mmm-container.mmm-section.mmm-vert-layout-s
          [:h2.mmm-h2
           (get-in category [:food-group-category/name locale])]
          [:div.mmm-cards
           (for [food-group groups]
             (let [the-name (get-in food-group [:food-group/name locale])]
               [:a.mmm-card.mmm-cols-d2m1.mmm-link {:href (urls/get-food-group-url locale the-name)}
                [:div.mmm-media
                 [:aside [:img.mmm-img {:src (:food-group/photo food-group)}]]
                 [:article.mmm-text
                  [:h3 the-name]
                  [:p (get-in food-group [:food-group/short-description locale])]]]]))
           (when (odd? (count groups))
             [:div.mmm-cols-d2m1.mmm-card {:style {:background "none"}}])]])]
      [:div.mmm-container.mmm-section
       (CompactSiteFooter)]]]))
