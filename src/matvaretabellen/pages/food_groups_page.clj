(ns matvaretabellen.pages.food-groups-page
  (:require [datomic-type-extensions.api :as d]
            [mattilsynet.design :as m]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.mashdown :as mashdown]
            [matvaretabellen.urls :as urls]
            [phosphor.icons :as icons]))

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
    (layout/layout
     context
     page
     [:head
      [:title [:i18n ::all-food-groups]]]
     [:body {:data-size "lg"}
      (layout/render-header
       {:locale locale
        :app/config (:app/config context)}
       urls/get-food-groups-url)
      [:div {:class (m/c :grid) :data-gap "12"}
       [:div.screen-sm-inline-pad {:class (m/c :grid :banner) :data-gap "8" :role "banner"}
        (layout/render-toolbar
         {:locale locale
          :crumbs [{:text [:i18n :i18n/search-label]
                    :url (urls/get-base-url locale)}
                   {:text [:i18n ::crumbs/all-food-groups]}]})
        [:div {:class (m/c :flex) :data-center "xl" :data-align "center"}
         [:div {:class (m/c :prose) :data-self "500"}
          [:h1 {:class (m/c :heading) :data-size "xl"} [:i18n ::all-food-groups]]
          [:p {:data-size "lg"} [:i18n ::prose
                                 {:food-count (d/q '[:find (count ?e) .
                                                     :where [?e :food/id]] food-db)
                                  :group-count (count food-groups)}]]
          [:div
           [:a {:class (m/c :button)
                :data-size "md"
                :data-variant "secondary"
                :href (urls/get-foods-excel-url locale)}
            (icons/render :phosphor.regular/arrow-down)
            [:i18n ::download-everything]]]]
         [:div.desktop {:data-self "300" :data-fixed ""}
          (layout/render-illustration "/images/illustrations/alle-matvaregrupper.svg")]]]
       (for [[category groups] (->> (group-by :food-group/category food-groups)
                                    (sort-by (comp :category/order first)))]
         [:div {:class (m/c :grid) :data-center "xl"}
          [:h2 {:class (m/c :heading) :data-size "md"}
           (get-in category [:category/name locale])]
          [:div {:class (m/c :grid) :data-items "450"}
           (for [food-group groups]
             (let [the-name (get-in food-group [:food-group/name locale])]
               [:a {:class (m/c :card :flex) :data-nowrap "" :data-gap "5" :data-items "200" :data-align "center" :href (urls/get-food-group-url locale the-name)}
                [:img {:src (:food-group/photo food-group)
                       :alt ""
                       :data-fixed ""
                       :style {:border-radius "var(--mtds-border-radius-md)"}}]
                [:div {:class (m/c :prose)}
                 [:h3 {:class (m/c :heading) :data-size "xs"} the-name]
                 [:p (mashdown/strip
                      (get-in food-group [:food-group/short-description locale]))]]]))]])]])))
