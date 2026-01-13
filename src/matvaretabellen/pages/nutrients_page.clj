(ns matvaretabellen.pages.nutrients-page
  (:require [datomic-type-extensions.api :as d]
            [mattilsynet.design :as m]
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
     page
     [:head
      [:title [:i18n ::all-nutrients]]]
     [:body {:data-size "lg"}
      (layout/render-header
       {:locale locale
        :app/config (:app/config context)}
       urls/get-nutrients-url)
      [:div {:class (m/c :grid) :data-gap "12"}
       [:div {:class (m/c :grid :banner :screen-sm-inline-pad)
              :data-gap "8"
              :role "banner"}
        (layout/render-toolbar
         {:locale locale
          :crumbs [{:text [:i18n :i18n/search-label]
                    :url (urls/get-base-url locale)}
                   {:text [:i18n ::crumbs/all-nutrients]}]})
        [:div {:class (m/c :flex) :data-center "xl" :data-align "center"}
         [:div {:class (m/c :prose) :data-self "500"}
          [:h1 {:class (m/c :heading) :data-size "xl"} [:i18n ::all-nutrients]]
          [:p {:data-size "lg"} [:i18n ::prose
                                 {:count (count nutrients)}]]]
         [:div.desktop {:data-self "300" :data-fixed ""}
          (layout/render-illustration "/images/illustrations/alle-naeringsstoffer.svg")]]]
       (for [[category groups] (->> nutrients
                                    (filter :nutrient/category)
                                    (group-by :nutrient/category)
                                    (sort-by (comp :category/order first)))]
         [:div {:class (m/c :grid) :data-center "xl"}
          [:h2 {:class (m/c :heading) :data-size "md"}
           (get-in category [:category/name locale])]
          [:div {:class (m/c :grid) :data-items "450"}
           (for [nutrient (sort-by :category/order groups)]
             (let [the-name (get-in nutrient [:nutrient/name locale])]
               [:a {:class (m/c :card :flex) :data-nowrap "" :data-gap "5" :data-items "200" :data-align "center" :href (urls/get-nutrient-url locale the-name)}
                [:img {:src (:nutrient/photo nutrient)
                       :alt ""
                       :data-fixed ""
                       :style {:border-radius "var(--mtds-border-radius-md)"}}]
                [:div {:class (m/c :prose)}
                 [:h3 {:class (m/c :heading) :data-size "xs"} the-name]
                 [:p (get-in nutrient [:nutrient/short-description locale])]]]))]])]])))

(comment

  (def conn matvaretabellen.dev/conn)

  (d/entity (d/db conn) [:nutrient/id "Protein"])


  )
