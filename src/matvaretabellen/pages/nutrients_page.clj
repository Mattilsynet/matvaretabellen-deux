(ns matvaretabellen.pages.nutrients-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.urls :as urls]))

(defn embellish-nutrient [nutrient app-db]
  (-> (into {} nutrient)
      (into (d/entity app-db [:nutrient/id (:nutrient/id nutrient)]))
      (update :nutrient/category #(d/entity app-db %))))

(defn render [context food-db page]
  (let [app-db (:app/db context)
        nutrients (for [eid (d/q '[:find [?e ...]
                                   :where
                                   [?e :nutrient/id]
                                   (not [?e :nutrient/parent])]
                                 food-db)]
                    (embellish-nutrient (d/entity food-db eid) app-db))
        locale (:page/locale page)]
    (layout/layout
     context
     [:head
      [:title [:i18n ::all-nutrients]]]
     [:body
      (layout/render-header locale urls/get-nutrients-url)
      [:div.mmm-themed.mmm-brand-theme1
       (layout/render-toolbar
        {:locale locale
         :crumbs [{:text [:i18n :i18n/search-label]
                   :url (urls/get-base-url locale)}
                  {:text [:i18n ::crumbs/all-nutrients]}]})
       [:div.mmm-container.mmm-section.mmm-mvxl
        [:div.mmm-media
         [:article.mmm-vert-layout-m
          [:h1.mmm-h1 [:i18n ::all-nutrients]]
          [:div.mmm-text.mmm-preamble
           [:p [:i18n ::prose
                {:count (count nutrients)}]]]]
         [:aside.mmm-desktop {:style {:flex-basis "40%"}}
          [:img {:src "/images/illustrations/alle-naeringsstoffer.svg"
                 :width 300}]]]]]
      [:div.mmm-themed.mmm-brand-theme2
       (for [[category groups] (->> nutrients
                                    (filter :nutrient/category)
                                    (group-by :nutrient/category)
                                    (sort-by (comp :category/order first)))]
         [:div.mmm-container.mmm-section.mmm-vert-layout-s.mmm-mobile-phn
          [:h2.mmm-h2.mmm-mobile-container-p
           (get-in category [:category/name locale])]
          [:div.mmm-cards
           (for [nutrient (sort-by :category/order groups)]
             (let [the-name (get-in nutrient [:nutrient/name locale])]
               [:a.mmm-card.mmm-cols-d2m1.mmm-link {:href (urls/get-nutrient-url locale the-name)}
                [:div.mmm-media
                 [:aside [:img.mvt-card-img {:src (:nutrient/photo nutrient)}]]
                 [:article.mmm-text
                  [:h3 the-name]
                  [:p (get-in nutrient [:nutrient/short-description locale])]]]]))
           (when (odd? (count groups))
             [:div.mmm-cols-d2m1.mmm-card {:style {:background "none"}}])]])]])))

(comment

  (def conn matvaretabellen.dev/conn)

  (d/entity (d/db conn) [:nutrient/id "Protein"])


  )
