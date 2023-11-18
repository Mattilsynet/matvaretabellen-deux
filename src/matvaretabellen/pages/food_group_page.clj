(ns matvaretabellen.pages.food-group-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.components.comparison :as comparison]
            [matvaretabellen.food :as food]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.mashdown :as mashdown]
            [matvaretabellen.pages.food-page :as food-page]
            [matvaretabellen.urls :as urls]
            [mmm.components.button :refer [Button]]))

(def filter-panel-id "filter-panel")

(defn render-food-group-links [app-db locale current food-groups]
  (when-let [food-groups (->> food-groups
                              (sort-by #(food/food-group->sort-key app-db %))
                              seq)]
    [:ul.mmm-ul.mmm-unadorned-list
     (for [group food-groups]
       [:li
        (if (= group current)
          (list [:strong [:i18n :i18n/lookup (:food-group/name group)]]
                (render-food-group-links app-db locale current (:food-group/_parent group)))
          [:a.mmm-link {:href (urls/get-food-group-url locale group)}
           [:i18n :i18n/lookup (:food-group/name group)]])])]))

(defn render-filter-links [app-db locale target food-group]
  (->> (or (:food-group/_parent food-group)
           (food/get-food-groups (d/entity-db food-group)))
       (render-food-group-links app-db locale target)))

(defn get-back-link [locale food-group]
  (if-let [parent (:food-group/parent food-group)]
    {:url (urls/get-food-group-url locale parent)
     :text [:i18n :i18n/lookup (:food-group/name parent)]}
    {:url (urls/get-food-groups-url locale)
     :text [:i18n ::food-groups]}))

(defn render-sidebar [app-db food-group foods locale]
  (let [target (or (:food-group/parent food-group) food-group)]
    [:div.mmm-col.mmm-desktop {:id filter-panel-id}
     [:div.mmm-sidebar-content
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
          [:div.mmm-divider.mmm-bottom-divider.mmm-vert-layout-m.mmm-mbm
           [:div.mmm-mobile.mmm-pos-tr.mmm-mts
            (layout/render-sidebar-close-button filter-panel-id)]
           (let [{:keys [url text]} (get-back-link locale food-group)]
             [:h2.mmm-h5 [:a.mmm-link {:href url} text]])
           links])
        (let [food-groups (:food-group/_parent food-group)]
          [:div.mmm-divider.mmm-vert-layout-m.mmm-bottom-divider
           [:div.mmm-mobile.mmm-pos-tr.mmm-mts
            (layout/render-sidebar-close-button filter-panel-id)]
           [:h2.mmm-h5
            [:a.mmm-link {:href (urls/get-food-groups-url locale)}
             [:i18n ::food-groups]]]
           (food/render-filter-data food-groups)
           (food/render-food-group-list app-db food-groups (set foods) locale)]))]]))

(defn prepare-foods-table [locale foods]
  {:headers [{:text [:i18n ::food]}
             {:text [:i18n ::compare]
              :class :mmm-td-min}]
   :id "filtered-table"
   :rows (for [food foods]
           {:data-id (:food-group/id (:food/food-group food))
            :cols [{:text [:a.mmm-link {:href (urls/get-food-url locale food)}
                           [:i18n :i18n/lookup (:food/name food)]]}
                   (comparison/render-toggle-cell food locale)]})})

(defn render [context db page]
  (let [locale (:page/locale page)
        food-group (d/entity (:foods/db context)
                             [:food-group/id (:page/food-group-id page)])
        details (d/entity (:app/db context)
                          [:food-group/id (:page/food-group-id page)])
        foods (->> (food/get-all-food-group-foods food-group)
                   (sort-by (comp locale :food/name)))]
    (layout/layout
     context
     [:head
      [:title (get-in food-group [:food-group/name locale])]]
     [:body
      (layout/render-header locale #(urls/get-food-group-url % food-group))
      [:div.mmm-themed.mmm-brand-theme1
       (layout/render-toolbar
        {:locale locale
         :crumbs [{:text [:i18n :i18n/search-label]
                   :url (urls/get-base-url locale)}
                  food-group]})
       [:div.mmm-container.mmm-section.mmm-mvxl
        [:div.mmm-media
         [:article.mmm-vert-layout-m
          [:div [:h1.mmm-h1 (get-in food-group [:food-group/name locale])]
           [:i18n :i18n/number-of-foods
            {:count (count foods)}]]
          [:div.mmm-text.mmm-preamble
           [:p (mashdown/render
                db locale
                (or (get-in details [:food-group/long-description locale])
                    (get-in details [:food-group/short-description locale])))]]
          [:div
           (Button {:text [:i18n ::download-these]
                    :href (urls/get-food-group-excel-url locale food-group)
                    :icon :fontawesome.solid/arrow-down
                    :inline? true
                    :secondary? true})]]
         [:aside.mmm-desktop {:style {:flex-basis "40%"}}
          [:img {:src (:food-group/illustration details)
                 :width 300}]]]]]

      (let [sidebar (render-sidebar (:app/db context) food-group foods locale)]
        [:div.mmm-container.mmm-section.mmm-mobile-phn
         [:div.mmm-flex.mmm-mobile-container-p
          (when sidebar
            (layout/render-sidebar-filter-button filter-panel-id))]
         [:div.mmm-cols.mmm-cols-d1_2
          sidebar
          [:div.mmm-col
           (->> (prepare-foods-table locale foods)
                food-page/render-table)]]])

      (comparison/render-comparison-drawer locale)])))
