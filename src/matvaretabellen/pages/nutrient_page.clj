(ns matvaretabellen.pages.nutrient-page
  (:require [datomic-type-extensions.api :as d]
            [mattilsynet.design :as mtds]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.food :as food]
            [matvaretabellen.food-group :as food-group]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.mashdown :as mashdown]
            [matvaretabellen.nutrient :as nutrient]
            [matvaretabellen.pages.food-page :as food-page]
            [matvaretabellen.ui.comparison :as comparison]
            [matvaretabellen.urls :as urls]
            [phosphor.icons :as icons]))

(def filter-panel-id "filter-panel")

(def aria-sort-order
  {:sort.order/asc "ascending"
   :sort.order/desc "descending"})

(defn prepare-foods-table [nutrient {:page/keys [locale sort-order]} foods]
  {:headers [{:text [:i18n ::food]}
             {:text [:button {:type "button"} [:i18n ::amount]]
              :aria-sort (aria-sort-order sort-order)
              :data-sort-by "data-value"
              :data-sort-type "number"
              :style {:width "var(--mtds-30)"}}
             {:text [:i18n ::compare]
              :style {:width "var(--mtds-26)"}}]
   :id "filtered-table"
   :classes [:mvt-filtered-table :mvt-sortable-table]
   :rows (for [food foods]
           {:data-id (:food-group/id (:food/food-group food))
            :cols
            [{:text [:a {:href (urls/get-food-url locale food)}
                     [:i18n :i18n/lookup (:food/name food)]]}
             {:text (food/get-nutrient-quantity food (:nutrient/id nutrient))
              :data-justify "end"
              :data-numeric ""}
             (comparison/render-toggle-cell food locale)]})})

(defn render-nutrient-foods-table
  "The initial idea was to list a limit amount of foods - say 100. This made it
  clear that for some nutrients, food number 100 still had a high portion of
  said nutrient. So I was curious about number 101.

  Then I figured, let's have a g/100g cutoff, like ... 20? 15? 10? VERY hard to
  find a reasonable cutoff, AND we effectively only cut a few foods, while also
  making it impossible to find the food with the least amount.

  Thus: list of all foods containing the nutrient in question."
  [nutrient foods page]
  (->> (prepare-foods-table nutrient page foods)
       food-page/render-table))

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
    [:ul {:class (mtds/classes :grid) :data-gap "1"}
     (for [n nutrients]
       [:li
        (if (= n current)
          (list [:strong [:i18n :i18n/lookup (:nutrient/name n)]]
                (render-nutrient-links locale n current))
          [:a {:href (urls/get-nutrient-url locale n)}
           [:i18n :i18n/lookup (:nutrient/name n)]])])]))

(defn render-sidebar [app-db nutrient foods locale]
  (let [target (or (:nutrient/parent nutrient) nutrient)]
    [:div {:class (mtds/classes :grid) :data-gap "8" :id filter-panel-id}
     (when-let [links (render-nutrient-links locale target nutrient)]
       [:div {:class (mtds/classes :grid)}
        (let [{:keys [url text]} (get-back-link locale nutrient)]
          [:h2 {:class (mtds/classes :heading) :data-size "xs"}
           [:a {:href url} text]])
        links])
     [:div {:class (mtds/classes :grid)}
      [:h2 {:class (mtds/classes :heading) :data-size "xs"}
       [:a {:href (urls/get-food-groups-url locale)}
        [:i18n ::food-groups]]]
      (food-group/render-food-group-filters
       app-db
       (food-group/get-food-groups (d/entity-db nutrient))
       foods
       locale)]]))

(defn render [context db page]
  (let [nutrient (d/entity (:foods/db context) [:nutrient/id (:page/nutrient-id page)])
        locale (:page/locale page)
        nutrient-name (get (:nutrient/name nutrient) locale)
        foods (nutrient/get-foods-by-nutrient-density nutrient locale (:page/sort-order page))]
    (layout/layout
     context
     page
     [:head
      [:title nutrient-name]]
     [:body {:data-size "lg"}
      (layout/render-header
       {:locale locale
        :app/config (:app/config context)}
       #(urls/get-nutrient-url % nutrient))
      [:div {:class (mtds/classes :grid) :data-gap "12"}
       [:div.screen-sm-inline-pad
        {:class (mtds/classes :grid :banner)
         :data-gap "8"
         :role "banner"}
        (layout/render-toolbar
         {:locale locale
          :crumbs [{:text [:i18n ::crumbs/all-nutrients]
                    :url (urls/get-nutrients-url locale)}
                   {:text nutrient-name}]})
        (let [details (d/entity (:app/db context) [:nutrient/id (:nutrient/id nutrient)])
              desc (get-in details [:nutrient/long-description locale])
              illustration (:nutrient/illustration details)]
          [:div {:class (mtds/classes :flex)
                 :data-center "xl"
                 :data-align "center"}
           [:div {:class (mtds/classes :prose) :data-self "500"}
            [:h1 {:class (mtds/classes :heading) :data-size "xl"} nutrient-name]
            (when (seq foods)
              [:small [:i18n :i18n/number-of-foods {:count (count foods)}]])
            (when desc
              [:p {:data-size "lg"} (if (string? desc)
                                      (mashdown/render db locale desc)
                                      desc)])
            (when (seq foods)
              [:div
               [:a {:class (mtds/classes :button)
                    :data-variant "secondary"
                    :data-size "md"
                    :href (urls/get-nutrient-excel-url locale nutrient)}
                (icons/render :phosphor.regular/arrow-down)
                [:i18n ::download-these]]])]
           (when (and desc illustration) ;; looks horrible without text
             [:div.desktop {:data-self "300" :data-fixed ""}
              (layout/render-illustration illustration)])])]

       (when (seq foods)
         (let [sidebar (render-sidebar (:app/db context) nutrient foods locale)]
           [:div {:class (mtds/classes :flex) :data-items "300" :data-center "xl"}
            [:div {:data-fixed "" :data-size "md"}
             sidebar]
            [:div
             (render-nutrient-foods-table nutrient foods page)]]))

       (comparison/render-comparison-drawer locale)]])))

(comment

  (def conn matvaretabellen.dev/conn)

  (->> (d/entity (d/db conn) [:nutrient/id "Fiber"])
       nutrient/get-foods-by-nutrient-density
       (map (comp :nb :food/name))
       count))
