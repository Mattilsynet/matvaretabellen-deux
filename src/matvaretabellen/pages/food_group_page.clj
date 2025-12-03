(ns matvaretabellen.pages.food-group-page
  (:require [datomic-type-extensions.api :as d]
            [mattilsynet.design :as mtds]
            [matvaretabellen.components.comparison :as comparison]
            [matvaretabellen.food-group :as food-group]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.mashdown :as mashdown]
            [matvaretabellen.pages.food-page :as food-page]
            [matvaretabellen.urls :as urls]
            [phosphor.icons :as icons]))

(def filter-panel-id "filter-panel")

(defn render-food-group-links [app-db locale current food-groups]
  (when-let [food-groups (->> food-groups
                              (sort-by #(food-group/food-group->sort-key app-db %))
                              seq)]
    [:ul {:class (mtds/classes :grid) :data-gap "1"}
     (for [group food-groups]
       [:li
        (if (= group current)
          (list [:strong [:i18n :i18n/lookup (:food-group/name group)]]
                (render-food-group-links app-db locale current (:food-group/_parent group)))
          [:a {:href (urls/get-food-group-url locale group)}
           [:i18n :i18n/lookup (:food-group/name group)]])])]))

(defn render-filter-links [app-db locale target food-group]
  (->> (or (:food-group/_parent food-group)
           (food-group/get-food-groups (d/entity-db food-group)))
       (render-food-group-links app-db locale target)))

(defn get-back-link [locale food-group]
  (if-let [parent (:food-group/parent food-group)]
    {:url (urls/get-food-group-url locale parent)
     :text [:i18n :i18n/lookup (:food-group/name parent)]}
    {:url (urls/get-food-groups-url locale)
     :text [:i18n ::food-groups]}))

(defn render-sidebar [app-db food-group foods locale]
  (let [target (or (:food-group/parent food-group) food-group)]
    [:div.mvt-food-group-filters {:id filter-panel-id}
     ;; Sub groups don't make for interesting filtering options, as they don't
     ;; list any foods above their level in the hierarchy.
     ;;
     ;; Food groups without sub groups also don't make interesting filtering
     ;; options.
     ;;
     ;; In both case we offer links to other food groups instead.
     (if (or (:food-group/parent food-group)
             (empty? (:food-group/_parent food-group)))
       (when-let [links (render-filter-links app-db locale target food-group)]
         [:div {:class (mtds/classes :grid)}
          (let [{:keys [url text]} (get-back-link locale food-group)]
            [:h2 {:class (mtds/classes :heading) :data-size "xs"}
             [:a {:href url} text]])
          links])
       [:div {:class (mtds/classes :grid)}
        [:h2 {:class (mtds/classes :heading) :data-size "xs"}
         [:a {:href (urls/get-food-groups-url locale)}
          [:i18n ::food-groups]]]
        (food-group/render-food-group-filters app-db (:food-group/_parent food-group) foods locale)])]))

(defn prepare-foods-table [locale foods]
  {:headers [{:text [:i18n ::food]}
             {:text [:i18n ::compare]
              :style {:width "1px"}}]
   :id "filtered-table"
   :classes [:mvt-filtered-table]
   :rows (for [food foods]
           {:data-id (:food-group/id (:food/food-group food))
            :cols [{:text [:a {:href (urls/get-food-url locale food)}
                           [:i18n :i18n/lookup (:food/name food)]]}
                   (comparison/render-toggle-cell food locale)]})})

(defn render [context db page]
  (let [locale (:page/locale page)
        food-group (d/entity (:foods/db context)
                             [:food-group/id (:page/food-group-id page)])
        details (d/entity (:app/db context)
                          [:food-group/id (:page/food-group-id page)])
        foods (->> (food-group/get-all-food-group-foods food-group)
                   (sort-by (comp locale :food/name)))]
    (layout/layout
     context
     page
     [:head
      [:title (get-in food-group [:food-group/name locale])]]
     [:body {:data-size "lg"}
      (layout/render-header
       {:locale locale
        :app/config (:app/config context)}
       #(urls/get-food-group-url % food-group))
      [:div {:class (mtds/classes :grid) :data-gap "12"}
       [:div {:class (mtds/classes :grid :banner) :data-gap "8" :role "banner"}
        (layout/render-toolbar
         {:locale locale
          :crumbs [{:text [:i18n :i18n/search-label]
                    :url (urls/get-base-url locale)}
                   food-group]})
        [:div {:class (mtds/classes :flex) :data-center "xl" :data-align "center"}
         [:div {:class (mtds/classes :prose) :data-self "500"}
          [:h1 {:class (mtds/classes :heading) :data-size "xl"} (get-in food-group [:food-group/name locale])]
          [:small
           [:i18n :i18n/number-of-foods
            {:count (count foods)}]]
          [:p {:data-size "lg"} (mashdown/render
                                 db locale
                                 (or (get-in details [:food-group/long-description locale])
                                     (get-in details [:food-group/short-description locale])))]
          [:div
           [:a {:class (mtds/classes :button)
                :data-variant "secondary"
                :data-size "md"
                :href (urls/get-food-group-excel-url locale food-group)}
            (icons/render :phosphor.regular/arrow-down)
            [:i18n ::download-these]]]]
         [:div.desktop {:data-self "300" :data-fixed ""}
          (layout/render-illustration (:food-group/illustration details))]]]

       (let [sidebar (render-sidebar (:app/db context) food-group foods locale)]
         [:div {:class (mtds/classes :flex) :data-items "300" :data-center "xl"}
          [:div {:data-fixed "" :data-size "md"}
           sidebar]
          [:div
           (->> (prepare-foods-table locale foods)
                food-page/render-table)]])

       (comparison/render-comparison-drawer locale)]])))
