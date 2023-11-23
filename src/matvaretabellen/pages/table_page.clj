(ns matvaretabellen.pages.table-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.food :as food]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.nutrient :as nutrient]
            [matvaretabellen.pages.food-page :as food-page]
            [matvaretabellen.urls :as urls]
            [mmm.components.button :refer [Button]]
            [mmm.components.checkbox :refer [Checkbox]]))

(def default-checked #{"Fett" "Karbo" "Protein" "Fiber"})

(defn prepare-foods-table [app-db locale nutrients foods]
  {:headers (concat [{:text [:i18n ::food]}
                     {:text [:i18n ::energy-kj]
                      :class [:mmm-nbr :mmm-tar]}
                     {:text [:i18n ::energy-kcal]
                      :class [:mmm-nbr :mmm-tar]}]
                    (for [nutrient nutrients]
                      {:text (food-page/get-nutrient-link app-db locale nutrient)
                       :data-id (:nutrient/id nutrient)
                       :class (when (not (default-checked (:nutrient/id nutrient)))
                                [:mmm-hidden])}))
   :id "filtered-table"
   :rows (for [food foods]
           (concat
            [{:text [:a.mmm-link {:href (urls/get-food-url locale food)}
                     [:i18n :i18n/lookup (:food/name food)]]}
             {:text (food-page/get-kj food)
              :class :mmm-tar}
             {:text (food-page/get-kcal food)
              :class :mmm-tar}]
            (for [nutrient nutrients]
              {:text (food/get-nutrient-quantity food (:nutrient/id nutrient))
               :class (cond-> [:mmm-tar :mmm-nbr :mvt-amount]
                        (not (default-checked (:nutrient/id nutrient)))
                        (conj :mmm-hidden))
               :data-id (:nutrient/id nutrient)})))})

(defn render-nutrient-filter-list [selectable-ids nutrients]
  (when (seq nutrients)
    [:ul.mmm-ul.mmm-unadorned-list
     (for [nutrient nutrients]
       [:li
        (if (selectable-ids (:nutrient/id nutrient))
          (Checkbox
           {:data-filter-id (:nutrient/id nutrient)
            :label [:i18n :i18n/lookup (:nutrient/name nutrient)]
            :checked? (default-checked (:nutrient/id nutrient))})
          [:strong [:i18n :i18n/lookup (:nutrient/name nutrient)]])
        (render-nutrient-filter-list
         selectable-ids
         (nutrient/sort-by-preference (:nutrient/_parent nutrient)))])]))

(defn render-nutrient-filters [selectable-ids nutrients]
  (let [left-col? (comp #{"Fett" "FatSolubleVitamins"} :nutrient/id)]
    (list
     [:div.mmm-col.mmm-vert-layout-m
      (->> (filter left-col? nutrients)
           (render-nutrient-filter-list selectable-ids))]
     [:div.mmm-col.mmm-vert-layout-m
      (for [nutrient (remove left-col? (nutrient/sort-by-preference nutrients))]
        (render-nutrient-filter-list selectable-ids [nutrient]))])))

(defn render-column-settings [foods-db nutrients]
  (let [used-nutrients (set (map :nutrient/id nutrients))]
    [:div.mmm-divider.mmm-vert-layout-m.mmm-bottom-divider
     [:h2.mmm-h5 [:a.mmm-link {:data-toggle-target "#filter-panel"} [:i18n ::columns]]]
     [:div.mmm-cols.mmm-twocols.mmm-hidden#filter-panel
      (->> (d/q '[:find [?n ...]
                  :where
                  [?n :nutrient/id]
                  (not [?n :nutrient/parent])]
                foods-db)
           (map #(d/entity foods-db %))
           (render-nutrient-filters used-nutrients))]]))

(defn render [context page]
  (layout/layout
   context
   page
   [:head
    [:title [:i18n ::page-title]]
    [:meta
     {:property "og:description"
      :content [:i18n ::open-graph-description]}]]
   [:body
    (layout/render-header (:page/locale page) urls/get-table-url)
    [:div.mmm-themed.mmm-brand-theme1
     (layout/render-toolbar
      {:locale (:page/locale page)
       :crumbs [{:text [:i18n :i18n/search-label]
                 :url (urls/get-base-url (:page/locale page))}
                {:text [:i18n ::page-title]}]})
     [:div.mmm-container.mmm-section
      [:div.mmm-media
       [:article.mmm-vert-layout-m
        [:div.mmm-vert-layout-s
         [:h1.mmm-h1 [:i18n ::page-title]]]
        [:div
         (Button {:text [:i18n ::download]
                  :href (urls/get-foods-excel-url (:page/locale page))
                  :icon :fontawesome.solid/arrow-down
                  :inline? true
                  :secondary? true})]]]]]
    (let [nutrients (->> (nutrient/get-used-nutrients (:foods/db context))
                         nutrient/sort-by-preference)]
      [:div.mmm-container.mmm-section.mmm-mobile-phn.mmm-sidescroller
       (render-column-settings (:foods/db context) nutrients)
       (->> (food/get-foods (:foods/db context))
            (sort-by (comp (:page/locale page) :food/name))
            (prepare-foods-table (:app/db context) (:page/locale page) nutrients)
            food-page/render-table)])]))
