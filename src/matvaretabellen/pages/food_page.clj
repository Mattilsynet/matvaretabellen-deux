(ns matvaretabellen.pages.food-page
  (:require [broch.core :as b]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.components.toc :refer [Toc]]
            [matvaretabellen.crumbs :as crumbs]
            [mt-designsystem.components.breadcrumbs :refer [Breadcrumbs]]
            [mt-designsystem.components.site-header :refer [SiteHeader]]))

(defn get-nutrient-grams [food id]
  (->> (:food/constituents food)
       (filter (comp #{id} :nutrient/id :constituent/nutrient))
       first
       :measurement/quantity
       b/num))

(defn prepare-nutrition-table [food]
  {:headers [[:i18n ::nutrients] [:i18n ::amount-grams]]
   :rows [[[:i18n ::total-fat] (get-nutrient-grams food "Fett")]
          [[:i18n ::total-carbs] (get-nutrient-grams food "Karbo")]
          [[:i18n ::total-protein] (get-nutrient-grams food "Protein")]
          [[:i18n ::total-water] (get-nutrient-grams food "Vann")]
          [[:i18n ::total-fiber] (get-nutrient-grams food "Fiber")]
          [[:i18n ::total-alcohol] (get-nutrient-grams food "Alko")]]})

(defn render-table [{:keys [headers rows]}]
  [:table.mvt-table
   [:thead
    [:tr
     (for [header headers]
       [:th header])]]
   [:tbody
    (for [row rows]
      [:tr
       (for [cell row]
         [:td cell])])]])

(defn render [context _db page]
  (let [food (d/entity (:foods/db context) [:food/id (:food/id page)])
        locale (:page/locale page)
        food-name (get-in food [:food/name locale])]
    [:html
     [:body
      (SiteHeader {:home-url "/"})
      [:div
       [:div.mvt-hero-banner
        [:div.container
         (Breadcrumbs
          {:links (crumbs/crumble locale
                                  (:food/food-group food)
                                  {:text food-name})})]]
       [:div.mvt-hero-banner
        [:div.container
         [:div {:style {:display "flex"}}
          [:div {:style {:flex "1"}}
           [:h1.h1 food-name]
           [:div.intro.mtl
            [:div [:i18n ::food-id {:id (:food/id food)}]]
            [:div [:i18n ::category {:category (get-in food [:food/food-group :food-group/name locale])}] ]
            [:div [:i18n ::latin-name {:food/latin-name (:food/latin-name food)}]]]]
          (Toc {:title [:i18n ::toc-title]
                :contents [{:title [:i18n ::nutrition-title]
                            :href "#naringsinnhold"
                            :contents [{:title [:i18n ::energy-title]
                                        :href "#energi"}
                                       {:title [:i18n ::fat-title]
                                        :href "#fett"}
                                       {:title [:i18n ::carbohydrates-title]
                                        :href "#karbohydrater"}
                                       {:title [:i18n ::vitamins-title]
                                        :href "#vitaminer"}
                                       {:title [:i18n ::minerals-title]
                                        :href "#mineraler"}]}
                           {:title [:i18n ::adi-title]
                            :href "#adi"}
                           {:title [:i18n ::description-title]
                            :href "#beskrivelse"}]})]]]
       [:div.container.mtl
        [:h2.h2 [:i18n ::nutrition-title]]]
       [:div.container.container-narrow.text#naringsinnhold
        [:h3.h3 [:i18n ::nutrition-heading]]
        [:ul.subtle-list
         [:li [:i18n ::energy
               {:kilo-joules (str (:measurement/quantity (:food/energy food)))
                :calories (:measurement/observation (:food/calories food))}]]
         [:li [:i18n ::edible-part
               {:pct (-> food :food/edible-part :measurement/percent)}]]]
        (render-table (prepare-nutrition-table food))

]]]]))
