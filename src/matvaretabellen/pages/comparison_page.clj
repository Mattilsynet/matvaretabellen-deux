(ns matvaretabellen.pages.comparison-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.food :as food]
            [matvaretabellen.pages.food-page :as food-page]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.footer :refer [CompactSiteFooter]]
            [mmm.components.site-header :refer [SiteHeader]]))

(defn render-header [locale]
  (SiteHeader
   {:home-url "/"
    :extra-link {:text [:i18n :i18n/other-language]
                 :url (urls/get-comparison-url ({:en :nb :nb :en} locale))}}))

(defn render-breadcrumbs [locale]
  [:div.mmm-container.mmm-section
   (Breadcrumbs
    {:links (crumbs/crumble
             locale
             {:text [:i18n ::crumbs/search-label]
              :url (urls/get-base-url locale)}
             {:text [:i18n ::compare-foods]})})])

(defn render-top-banner [locale _context]
  [:div.mmm-themed.mmm-brand-theme1
   (render-breadcrumbs locale)
   [:div.mmm-container.mmm-section
    [:div.mmm-media-d.mmm-media-at
     [:article.mmm-vert-layout-m
      [:h1.mmm-h1 [:i18n ::compare-foods]]
      [:p.mmm-p.mvtc-rating-summary.mmm-hidden [:i18n ::energy-summary]]
      [:p.mmm-p [:i18n ::diff-intro]]]
     (food-page/render-toc {:contents (drop-last 2 (food-page/get-toc-items))
                            :class :mmm-nbr})]]])

(defn prepare-energy-rows [food]
  [[{:text food-page/energy-label}
    {:text (food-page/get-kj food {:class "mvt-kj"})
     :class [:mmm-nbr :mvtc-energy]}]
   [{:text food-page/kcal-label}
    {:text (food-page/get-kcal food {:class "mvt-kcal"})
     :class [:mmm-nbr :mvtc-energy]}]
   [{:text [:i18n ::edible-part]}
    {:text (list [:span.mvtc-edible-part (-> food :food/edible-part :measurement/percent)] " %")
     :class [:mvt-amount]}]])

(defn get-nutrient-cols [app-db locale food nutrient]
  [{:text (food-page/get-nutrient-link app-db locale nutrient)}
   {:text (food-page/get-nutrient-quantity food (:nutrient/id nutrient))
    :class [:mvtc-nutrient]
    :data-nutrient-id (:nutrient/id nutrient)}])

(defn prepare-macro-rows [app-db locale food]
  (into [{:class [:mmm-thead]
          :cols [{:text [:i18n ::food-page/nutrients]
                  :tag :th
                  :class [:mmm-sticky]}
                 {}]}]
        (for [id food-page/nutrition-table-row-ids]
          (->> (:constituent/nutrient (food/get-nutrient-measurement food id))
               (get-nutrient-cols app-db locale food)))))

(defn prepare-nutrient-rows [app-db locale {:keys [food nutrients group id]}]
  (concat
   [{:class [:mmm-thead]
     :cols [{:text (food-page/get-nutrient-link app-db locale group)
             :tag :th
             :class [:mmm-sticky]
             :id id}
            {}]}]
   (for [nutrient nutrients]
     (get-nutrient-cols app-db locale food nutrient))
   (mapcat
    #(when-let [nutrients (food/get-nutrients food (:nutrient/id %))]
       (prepare-nutrient-rows
        app-db
        locale
        {:food food
         :nutrients nutrients
         :group %}))
    nutrients)))

(defn render-page [context {:page/keys [locale]}]
  (let [app-db (:app/db context)
        food (d/entity (:foods/db context) [:food/id "05.448"])]
    [:html.mmm
     [:body#comparison
      (render-header locale)
      (render-top-banner locale context)
      [:div.mmm-container-focused.mmm-section.mmm-mobile-phn.mmm-vert-layout-m
       {:id "container"}
       (food-page/render-table
        {:headers {:cols [{:text [:i18n ::composition]
                           :class [:mmm-sticky]}
                          {:text ""
                           :class [:mmm-sticky :mmm-nbr :mvt-amount :mvtc-food-name]}]
                   :class :mvtc-comparison}
         :classes [:mmm-table-hover]
         :rows (->> (concat
                     (prepare-energy-rows food)
                     (prepare-macro-rows app-db locale food)
                     (->> (assoc (food/get-nutrient-group food "Karbo") :id "karbohydrater")
                          (prepare-nutrient-rows app-db locale))
                     (->> (assoc (food/get-nutrient-group food "Fett") :id "fett")
                          (prepare-nutrient-rows app-db locale))
                     (->> (assoc (food/get-nutrient-group food "FatSolubleVitamins") :id "vitaminer")
                          (prepare-nutrient-rows app-db locale))
                     (->> (food/get-nutrient-group food "WaterSolubleVitamins")
                          (prepare-nutrient-rows app-db locale))
                     (->> (assoc (food/get-nutrient-group food "Minerals") :id "mineraler")
                          (prepare-nutrient-rows app-db locale))
                     (->> (assoc (food/get-nutrient-group food "TraceElements") :id "sporstoffer")
                          (prepare-nutrient-rows app-db locale)))
                    (map (fn [row]
                           (if (map? row)
                             (update row :class conj :mvtc-comparison)
                             {:class [:mvtc-comparison]
                              :cols row}))))})]

      (for [rating [:matvaretabellen.diff/similar
                    :matvaretabellen.diff/slight
                    :matvaretabellen.diff/moderate
                    :matvaretabellen.diff/significant
                    :matvaretabellen.diff/dramatic]]
        [:script {:type "text/i18n" :data-rating (name rating)}
         [:i18n rating]])

      [:script {:type "text/i18n" :data-k "and"}
       [:i18n ::and]]

      [:div.mmm-container.mmm-section
       (CompactSiteFooter)]]]))
