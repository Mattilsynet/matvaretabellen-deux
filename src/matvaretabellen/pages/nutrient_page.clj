(ns matvaretabellen.pages.nutrient-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.components.comparison :as comparison]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.food :as food]
            [matvaretabellen.food-group :as food-group]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.mashdown :as mashdown]
            [matvaretabellen.nutrient :as nutrient]
            [matvaretabellen.pages.food-page :as food-page]
            [matvaretabellen.urls :as urls]
            [mmm.components.button :refer [Button]]))

(def filter-panel-id "filter-panel")

(defn prepare-foods-table [nutrient locale foods]
  {:headers [{:text [:i18n ::food]}
             {:text [:i18n ::amount]
              :class "mmm-tar mmm-td-min"}
             {:text [:i18n ::compare]
              :class "mmm-td-min mmm-desktop"}]
   :id "filtered-table"
   :rows (for [food foods]
           {:data-id (:food-group/id (:food/food-group food))
            :cols
            [{:text [:a.mmm-link {:href (urls/get-food-url locale food)}
                     [:i18n :i18n/lookup (:food/name food)]]}
             {:text (food/get-nutrient-quantity food (:nutrient/id nutrient))
              :class "mmm-tar mmm-nbr"}
             (comparison/render-toggle-cell food locale [:mmm-desktop])]})})

(defn render-nutrient-foods-table
  "Really, ALL of the foods on one page? Well, not all of them, just the ones that
  have a non-zero amount of the nutrient in question.

  The initial idea was to list a limit amount of foods - say 100. This made it
  clear that for some nutrients, food number 100 still had a high portion of
  said nutrient. So I was curious about number 101.

  Then I figured, let's have a g/100g cutoff, like ... 20? 15? 10? VERY hard to
  find a reasonable cutoff, AND we effectively only cut a few foods, while also
  making it impossible to find the food with the least amount.

  Thus: list of all foods containing the nutrient in question."
  [nutrient foods locale]
  [:div.mmm-col
   (->> (prepare-foods-table nutrient locale foods)
        food-page/render-table)])

(defn get-back-link [locale nutrient]
  (if-let [parent (:nutrient/parent nutrient)]
    {:url (urls/get-nutrient-url locale parent)
     :text [:i18n :i18n/lookup (:nutrient/name parent)]}
    {:url (urls/get-nutrients-url locale)
     :text [:i18n ::nutrients]}))

(defn render-nutrient-links [locale parent current]
  (when-let [nutrients (->> (:nutrient/_parent parent)
                            nutrient/sort-by-preference
                            seq)]
    [:ul.mmm-ul.mmm-unadorned-list
     (for [n nutrients]
       [:li
        (if (= n current)
          (list [:strong [:i18n :i18n/lookup (:nutrient/name n)]]
                (render-nutrient-links locale n current))
          [:a.mmm-link {:href (urls/get-nutrient-url locale n)}
           [:i18n :i18n/lookup (:nutrient/name n)]])])]))

(defn render-sidebar [app-db nutrient foods locale]
  (let [target (or (:nutrient/parent nutrient) nutrient)]
    [:div.mmm-col.mmm-desktop {:id filter-panel-id}
     [:div.mmm-sidebar-content
      (when-let [links (render-nutrient-links locale target nutrient)]
        [:div.mmm-divider.mmm-vert-layout-m.mmm-mbm
         [:div.mmm-mobile.mmm-pos-tr.mmm-mts
          (layout/render-sidebar-close-button filter-panel-id)]
         (let [{:keys [url text]} (get-back-link locale nutrient)]
           [:h2.mmm-h5 [:a.mmm-link {:href url} text]])
         links])
      [:div.mmm-divider.mmm-vert-layout-m.mmm-bottom-divider
       [:div.mmm-mobile.mmm-pos-tr.mmm-mts
        (layout/render-sidebar-close-button filter-panel-id)]
       [:h2.mmm-h5
        [:a.mmm-link {:href (urls/get-food-groups-url locale)}
         [:i18n ::food-groups]]]
       (food-group/render-food-group-filters app-db (d/entity-db nutrient) foods locale)]]]))

(defn render [context db page]
  (let [nutrient (d/entity (:foods/db context) [:nutrient/id (:page/nutrient-id page)])
        locale (:page/locale page)
        nutrient-name (get (:nutrient/name nutrient) locale)
        foods (nutrient/get-foods-by-nutrient-density nutrient)]
    (layout/layout
     context
     page
     [:head
      [:title nutrient-name]]
     [:body
      (layout/render-header locale #(urls/get-nutrient-url % nutrient))
      [:div.mmm-themed.mmm-brand-theme1
       (layout/render-toolbar
        {:locale locale
         :crumbs [{:text [:i18n ::crumbs/all-nutrients]
                   :url (urls/get-nutrients-url locale)}
                  {:text nutrient-name}]})
       (let [details (d/entity (:app/db context) [:nutrient/id (:nutrient/id nutrient)])
             desc (get-in details [:nutrient/long-description locale])
             illustration (:nutrient/illustration details)]
         [:div.mmm-container.mmm-section
          [:div.mmm-media
           [:article.mmm-vert-layout-m
            [:div.mmm-vert-layout-s
             [:h1.mmm-h1 nutrient-name]
             (when (seq foods)
               [:p.mmm-p [:i18n :i18n/number-of-foods {:count (count foods)}]])]
            (when desc
              [:div.mmm-text.mmm-preamble
               [:p (if (string? desc)
                     (mashdown/render db locale desc)
                     desc)]])
            (when (seq foods)
              [:div
               (Button {:text [:i18n ::download-these]
                        :href (urls/get-nutrient-excel-url locale nutrient)
                        :icon :fontawesome.solid/arrow-down
                        :inline? true
                        :secondary? true})])]
           (when (and desc illustration) ;; looks horrible without text
             [:aside.mmm-desktop {:style {:flex-basis "40%"}}
              [:img {:src illustration :width 300}]])]])]

      (when (seq foods)
        (let [sidebar (render-sidebar (:app/db context) nutrient foods locale)]
          [:div.mmm-container.mmm-section.mmm-mobile-phn
           [:div.mmm-flex.mmm-flex-middle.mmm-mobile-container-p.mmm-mbm
            (when sidebar
              (layout/render-sidebar-filter-button filter-panel-id))
            [:p.mmm-p.mmm-tar.mmm-flex-grow
             [:i18n ::per-100g
              {:nutrient (get-in nutrient [:nutrient/name locale])}]]]
           [:div.mmm-cols.mmm-cols-d1_2
            sidebar
            (render-nutrient-foods-table nutrient foods locale)]]))

      (comparison/render-comparison-drawer locale)])))

(comment

  (def conn matvaretabellen.dev/conn)

  (->> (d/entity (d/db conn) [:nutrient/id "Fiber"])
       nutrient/get-foods-by-nutrient-density
       (map (comp :nb :food/name))
       count)


  )
