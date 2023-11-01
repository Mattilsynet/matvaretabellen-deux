(ns matvaretabellen.pages.food-page
  (:require [broch.core :as b]
            [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.select :refer [Select]]
            [mmm.components.site-header :refer [SiteHeader]]
            [mmm.components.toc :refer [Toc]]))

(def preferred-nutrient-order
  (reverse
   ["Mettet"
    "Enumet"
    "Flerum"
    "Trans"
    "Kolest"]))

(defn wrap-in-portion-span [num]
  [:span {:data-portion num} num])

(defn get-nutrient-grams [food id]
  (some->> (:food/constituents food)
           (filter (comp #{id} :nutrient/id :constituent/nutrient))
           first
           :measurement/quantity
           b/num
           wrap-in-portion-span))

(defn get-nutrient-parts [food nutrient-id]
  (->> (:food/constituents food)
       (map :constituent/nutrient)
       (filter (comp #{nutrient-id}
                     :nutrient/id
                     :nutrient/parent))
       (sort-by :nutrient/id)
       (sort-by #(- (.indexOf preferred-nutrient-order (:nutrient/id %))))))

(defn prepare-nutrition-table [food]
  {:headers [[:i18n ::nutrients] [:i18n ::amount-grams]]
   :rows [[[:i18n ::total-fat] (get-nutrient-grams food "Fett")]
          [[:i18n ::total-carbs] (get-nutrient-grams food "Karbo")]
          [[:i18n ::total-protein] (get-nutrient-grams food "Protein")]
          [[:i18n ::total-water] (get-nutrient-grams food "Vann")]
          [[:i18n ::total-fiber] (get-nutrient-grams food "Fiber")]
          [[:i18n ::total-alcohol] (get-nutrient-grams food "Alko")]]})

(defn prepare-fat-tables [food]
  (let [fats (get-nutrient-parts food "Fett")]
    (->> (concat
          [{:headers [[:i18n ::fat-composition-title] [:i18n ::amount-grams]]
            :rows (for [nutrient fats]
                    [[:i18n ::lookup (:nutrient/name nutrient)]
                     (get-nutrient-grams food (:nutrient/id nutrient))])}]
          (for [fat fats]
            (when-let [acids (seq (get-nutrient-parts food (:nutrient/id fat)))]
              {:headers [[:i18n ::lookup (:nutrient/name fat)] [:i18n ::amount-grams]]
               :rows (for [acid acids]
                       [[:i18n ::lookup (:nutrient/name acid)]
                        (get-nutrient-grams food (:nutrient/id acid))])})))
         (remove nil?))))

(defn render-table [{:keys [headers rows]}]
  [:table.mmm-table.mmm-nutrient-table.mmm-mbl.mmm-table-zebra
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
    [:html {:class "mmm"}
     [:body
      (SiteHeader {:home-url "/"})
      [:div
       [:div.mmm-themed.mmm-brand-theme1
        [:div.mmm-container.mmm-section
         (Breadcrumbs
          {:links (crumbs/crumble locale
                                  (:food/food-group food)
                                  {:text food-name})})]
        [:div.mmm-container.mmm-section
         [:div.mmm-media.mmm-media-at
          [:article.mmm-text
           [:h1 food-name]
           [:ul.mmm-unadorned-list
            [:li [:i18n ::food-id {:id (:food/id food)}]]
            [:li [:i18n ::category {:category (get-in food [:food/food-group :food-group/name locale])}]]
            [:li [:i18n ::latin-name {:food/latin-name (:food/latin-name food)}]]]]
          [:aside
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
                             :href "#beskrivelse"}]})]]]]
       [:div.mmm-section.mmm-container-focused
        [:div.mmm-desktop-flex.mmm-flex-bottom
         [:h2.mmm-h2.mmm-mbn#naringsinnhold [:i18n ::nutrition-title]]
         [:div
          [:p [:i18n ::portion-size]]
          (Select
           {:id "portion-selector"
            :class "mmm-input-m"
            :options (into [[:option {:value "100"} "100 gram"]]
                           (for [portion (:food/portions food)]
                             (let [grams (int (b/num (:portion/quantity portion)))]
                               [:option {:value grams} (str "1 " (str/lower-case (:portion-kind/name (:portion/kind portion)))
                                                            " (" grams " gram)")])))})]]
        [:h3.mmm-h3#energi [:i18n ::nutrition-heading]]
        [:ul.mmm-unadorned-list
         [:li [:i18n ::energy
               {:kilo-joules (str (:measurement/quantity (:food/energy food)))
                :calories (:measurement/observation (:food/calories food))}]]
         [:li [:i18n ::edible-part
               {:pct (-> food :food/edible-part :measurement/percent)}]]]
        (render-table (prepare-nutrition-table food))

        [:h3.mmm-h3#fett [:i18n ::fat-title]]
        (for [table (prepare-fat-tables food)]
          (render-table table))]]]]))
