(ns matvaretabellen.pages.comparison-page
  (:require [clojure.data.json :as json]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.food :as food]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.nutrient :as nutrient]
            [matvaretabellen.pages.food-page :as food-page]
            [matvaretabellen.statistics :as statistics]
            [matvaretabellen.ui.client-table :as client-table]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.button :refer [Button]]
            [mmm.components.tabs :refer [PillTabs]]))

(defn render-breadcrumbs [locale]
  [:div.mmm-container.mmm-section
   (Breadcrumbs
    {:links (crumbs/crumble
             locale
             {:text [:i18n :i18n/search-label]
              :url (urls/get-base-url locale)}
             {:text [:i18n ::compare-foods]})})])

(defn render-share-button [locale]
  (list
   (Button {:text [:i18n ::share-comparison]
            :href (urls/get-comparison-url locale)
            :data-receipt "#share-receipt"
            :secondary? true
            :icon :fontawesome.solid/share-from-square
            :inline? true
            :class [:mvtc-share :mmm-button-small]})
   [:span.mmm-hidden#share-receipt [:i18n ::url-copied]]))

(defn render-top-banner [locale _context]
  [:div.mmm-themed.mmm-brand-theme1
   (render-breadcrumbs locale)
   [:div.mmm-container.mmm-section
    [:h1.mmm-h1 [:i18n ::compare-foods]]]])

(defn prepare-energy-rows [food]
  [{:class [:mvtc-comparison]
    :id "energi"
    :data-compare-abs "energyKj"
    :cols [{:text food-page/energy-label}
           {:text (food-page/get-kj food {:class "mvt-kj"})
            :class [:mmm-nbr :mvtc-energy]}]}
   {:class [:mvtc-comparison]
    :data-compare-abs "energyKcal"
    :cols [{:text food-page/kcal-label}
           {:text (food-page/get-kcal food {:class "mvt-kcal"})
            :class [:mmm-nbr :mvtc-energy]}]}
   {:class [:mvtc-comparison]
    :data-compare-abs "ediblePart"
    :cols [{:text [:i18n ::edible-part]}
           {:text (list [:span.mvtc-edible-part (-> food :food/edible-part :measurement/percent)] " %")
            :class [:mvt-amount]}]}])

(defn get-nutrient-row [app-db locale food nutrient]
  {:data-nutrient-id (:nutrient/id nutrient)
   :cols [{:text (food-page/get-nutrient-link app-db locale nutrient)}
          {:text (food/get-nutrient-quantity food (:nutrient/id nutrient))
           :class [:mvtc-nutrient]
           :data-nutrient-id (:nutrient/id nutrient)}]})

(defn prepare-macro-rows [app-db locale food]
  (into [{:class [:mmm-thead]
          :cols [{:text [:i18n ::food-page/nutrients]
                  :tag :th
                  :class [:mmm-sticky]}
                 {}]}]
        (for [id food-page/nutrition-table-row-ids]
          (->> (:constituent/nutrient (food/get-nutrient-measurement food id))
               (get-nutrient-row app-db locale food)))))

(defn prepare-nutrient-rows [app-db locale {:keys [food nutrients group id]}]
  (concat
   [{:class [:mmm-thead]
     :cols [{:text (food-page/get-nutrient-link app-db locale group)
             :tag :th
             :class [:mmm-sticky]
             :id id}
            {}]}]
   (for [nutrient nutrients]
     (get-nutrient-row app-db locale food nutrient))
   (mapcat
    #(when-let [nutrients (food/get-nutrients food (:nutrient/id %))]
       (prepare-nutrient-rows
        app-db
        locale
        {:food food
         :nutrients nutrients
         :group %}))
    nutrients)))

(defn render-columnwise-comparison [context page]
  (let [app-db (:app/db context)
        food (d/entity (:foods/db context) [:food/id "05.448"])]
    (food-page/render-table
     {:headers {:cols [{:text [:i18n ::composition]
                        :class [:mmm-sticky]}
                       {:text ""
                        :class [:mmm-sticky :mmm-nbr :mvt-amount :mvtc-food-name]}]
                :class [:mvtc-comparison]}
      :classes [:mmm-table-hover]
      :id "columnwise-table"
      :rows (->> (concat
                  (prepare-energy-rows food)
                  (prepare-macro-rows app-db (:page/locale page) food)
                  (->> (assoc (food/get-nutrient-group food "Karbo") :id "karbohydrater")
                       (prepare-nutrient-rows app-db (:page/locale page)))
                  (->> (assoc (food/get-nutrient-group food "Fett") :id "fett")
                       (prepare-nutrient-rows app-db (:page/locale page)))
                  (->> (assoc (food/get-nutrient-group food "FatSolubleVitamins") :id "vitaminer")
                       (prepare-nutrient-rows app-db (:page/locale page)))
                  (->> (food/get-nutrient-group food "WaterSolubleVitamins")
                       (prepare-nutrient-rows app-db (:page/locale page)))
                  (->> (assoc (food/get-nutrient-group food "Minerals") :id "mineraler")
                       (prepare-nutrient-rows app-db (:page/locale page)))
                  (->> (assoc (food/get-nutrient-group food "TraceElements") :id "sporstoffer")
                       (prepare-nutrient-rows app-db (:page/locale page))))
                 (map (fn [row]
                        (if (map? row)
                          (update row :class conj :mvtc-comparison)
                          {:class [:mvtc-comparison]
                           :cols row}))))})))

(defn render-page [context page]
  (layout/layout
   context
   page
   [:head
    [:title [:i18n ::page-title]]]
   [:body#comparison
    (layout/render-header (:page/locale page) urls/get-comparison-url)
    (render-top-banner (:page/locale page) context)

    [:div.mmm-container-focused.mmm-mobile-mtn
     {:id "container"}
     [:div.mmm-mvl.mmm-mobile-container-p
      [:div.mmm-flex
       [:div.mmm-brand-theme2
        (PillTabs
         {:tabs [{:text [:i18n ::columnwise]
                  :selected? true
                  :data-tab-target "#columnwise"}
                 {:text [:i18n ::rowwise]
                  :data-tab-target "#rowwise"}]})]
       [:p.mmm-p.mmm-desktop (render-share-button (:page/locale page))]]]

     [:div.mmm-vert-layout-m#columnwise.mvtc-tab-target
      [:p.mmm-p [:i18n ::diff-intro]]
      (render-columnwise-comparison context page)]

     [:div#rowwise.mmm-hidden.mvtc-tab-target.mmm-vert-layout-m
      [:p.mmm-p (client-table/render-nutrients-toggle)]
      (client-table/render-column-settings (:foods/db context))
      (client-table/render-table-skeleton
       (:foods/db context)
       {:data-table-dataset "comparison"
        :id "rowwise-table"})]]

    (for [rating [:matvaretabellen.diff/similar
                  :matvaretabellen.diff/slight
                  :matvaretabellen.diff/moderate
                  :matvaretabellen.diff/significant
                  :matvaretabellen.diff/dramatic]]
      [:script {:type "text/i18n" :data-rating (name rating)}
       [:i18n rating]])

    [:script {:type "text/i18n" :data-k "and"}
     [:i18n ::and]]

    [:script.mvtc-statistics {:type "application/json"}
     (json/write-str (nutrient/get-nutrient-statistics (:foods/db context) statistics/get-median))]]))
