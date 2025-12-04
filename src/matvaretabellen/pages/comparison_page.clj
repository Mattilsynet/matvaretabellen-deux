(ns matvaretabellen.pages.comparison-page
  (:require [clojure.data.json :as json]
            [datomic-type-extensions.api :as d]
            [mattilsynet.design :as mtds]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.food :as food]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.nutrient :as nutrient]
            [matvaretabellen.pages.food-page :as food-page]
            [matvaretabellen.statistics :as statistics]
            [matvaretabellen.ui.client-table :as client-table]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [phosphor.icons :as icons]))

(defn render-breadcrumbs [locale]
  (Breadcrumbs
   {:links (crumbs/crumble
            locale
            {:text [:i18n :i18n/search-label]
             :url (urls/get-base-url locale)}
            {:text [:i18n ::compare-foods]})}))

(defn render-share-button [locale]
  (list
   [:a {:class (mtds/classes :button :mvtc-share)
        :data-variant "secondary"
        :popovertarget "share-receipt"
        :href (urls/get-comparison-url locale)}
    (icons/render :phosphor.regular/export)
    [:i18n ::share-comparison]]
   [:div#share-receipt {:class (mtds/classes :popover)
          :popover ""}
    [:i18n ::url-copied]]))

(defn render-top-banner [locale _context]
  [:div {:class (mtds/classes :grid :banner) :role "banner"}
   [:div {:class (mtds/classes :grid) :data-center "xl" :data-gap "8"}
    (render-breadcrumbs locale)
    [:h1 {:class (mtds/classes :heading) :data-size "xl"}
     [:i18n ::compare-foods]]]])

(defn prepare-energy-rows [food]
  [{:class [:mvtc-comparison]
    :id "energi"
    :data-compare-abs "energyKj"
    :cols [{:text food-page/energy-label}
           {:text (food-page/get-kj food {:class "mvt-kj"})
            :data-justify "end"
            :data-numeric ""
            :data-nowrap ""
            :class [:mvtc-energy]}]}
   {:class [:mvtc-comparison]
    :data-compare-abs "energyKcal"
    :cols [{:text food-page/kcal-label}
           {:text (food-page/get-kcal food {:class "mvt-kcal"})
            :data-justify "end"
            :data-numeric ""
            :data-nowrap ""
            :class [:mvtc-energy]}]}
   {:class [:mvtc-comparison]
    :data-compare-abs "ediblePart"
    :cols [{:text [:i18n ::edible-part]}
           {:text (list [:span.mvtc-edible-part (-> food :food/edible-part :measurement/percent)] " %")
            :data-justify "end"
            :data-numeric ""
            :data-nowrap ""
            :class [:mvt-amount]}]}])

(defn get-nutrient-row [app-db locale food nutrient]
  {:data-nutrient-id (:nutrient/id nutrient)
   :cols [{:text (food-page/get-nutrient-link app-db locale nutrient)}
          {:text (food/get-nutrient-quantity food (:nutrient/id nutrient))
           :data-justify "end"
           :data-numeric ""
           :data-nowrap ""
           :class [:mvtc-nutrient]
           :data-nutrient-id (:nutrient/id nutrient)}]})

(defn prepare-macro-rows [app-db locale food]
  (into [{:cols [{:text [:i18n ::food-page/nutrients]
                  :tag :th}
                 {}]}]
        (for [id food-page/nutrition-table-row-ids]
          (->> (:constituent/nutrient (food/get-nutrient-measurement food id))
               (get-nutrient-row app-db locale food)))))

(defn prepare-nutrient-rows [app-db locale {:keys [food nutrients group id]}]
  (concat
   [{:cols [{:text (food-page/get-nutrient-link app-db locale group)
             :tag :th
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
     {:headers {:cols [{:text [:i18n ::composition]}
                       {:text ""
                        :data-nowrap ""
                        :class [:mvt-amount :mvtc-food-name]}]
                :class [:mvtc-comparison]}
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
   [:body#comparison {:data-size "lg"}
    (layout/render-header
     {:locale (:page/locale page)
      :app/config (:app/config context)}
     urls/get-comparison-url)
    [:div {:class (mtds/classes :grid) :data-gap "12"}
     (render-top-banner (:page/locale page) context)

     [:div {:class (mtds/classes :grid) :data-center "xl"}
      [:div {:class (mtds/classes :flex)
             :data-align "center"
             :data-justify "space-between"
             :data-size "md"}
       [:fieldset {:class (mtds/classes :togglegroup)}
        [:label
         [:input {:type "radio" :name "comparison-view" :value "#columnwise" :checked ""}]
         [:i18n ::columnwise]]
        [:label
         [:input {:type "radio" :name "comparison-view" :value "#rowwise"}]
         [:i18n ::rowwise]]]
       [:div {:class (mtds/classes :flex)}
        (client-table/render-download-csv-button)
        (render-share-button (:page/locale page))]]]

     [:div.mvtc-tab-target#columnwise {:class (mtds/classes :grid) :data-center "xl"}
      [:p [:i18n ::diff-intro]]
      [:figure
       (render-columnwise-comparison context page)]]

     [:div#rowwise.mmm-hidden.mvtc-tab-target {:class (mtds/classes :grid) :data-center "xl"}
      [:div {:data-size "sm"} (client-table/render-nutrients-toggle)]
      (client-table/render-column-settings (:foods/db context))
      (client-table/render-table-skeleton
       (:foods/db context)
       {:data-table-dataset "comparison"
        :id "rowwise-table"})]

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
      (json/write-str (nutrient/get-nutrient-statistics (:foods/db context) statistics/get-median))]]]))
