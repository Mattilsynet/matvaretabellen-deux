(ns matvaretabellen.pages.food-page
  (:require [broch.core :as b]
            [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.food :as food]
            [matvaretabellen.nutrient :as nutrient]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.card :refer [DetailFocusCard]]
            [mmm.components.footer :refer [SiteFooter]]
            [mmm.components.select :refer [Select]]
            [mmm.components.site-header :refer [SiteHeader]]
            [mmm.components.toc :refer [Toc]]))

(defn wrap-in-portion-span [num]
  [:span {:data-portion num} num])

(defn get-calculable-quantity [measurement]
  (when-let [q (:measurement/quantity measurement)]
    (list (wrap-in-portion-span (b/num q)) " " (b/symbol q))))

(defn get-nutrient-quantity [food id]
  (some->> (:food/constituents food)
           (filter (comp #{id} :nutrient/id :constituent/nutrient))
           first
           get-calculable-quantity))

(defn prepare-nutrition-table [food]
  {:headers [[:i18n ::nutrients] [:i18n ::amount]]
   :rows [[[:i18n ::total-fat] (get-nutrient-quantity food "Fett")]
          [[:i18n ::total-carbs] (get-nutrient-quantity food "Karbo")]
          [[:i18n ::total-fiber] (get-nutrient-quantity food "Fiber")]
          [[:i18n ::total-protein] (get-nutrient-quantity food "Protein")]
          [[:i18n ::total-alcohol] (get-nutrient-quantity food "Alko")]
          [[:i18n ::total-water] (get-nutrient-quantity food "Vann")]]
   :classes ["mmm-nutrient-table"]})

(defn get-kj [food]
  (when (:measurement/quantity (:food/energy food))
    (get-calculable-quantity (:food/energy food))))

(defn energy [food]
  (concat
   (get-kj food)
   (when (:measurement/observation (:food/calories food))
     (list " (" (wrap-in-portion-span (:food/calories food)) " kcal)"))))

(defn prepare-macro-highlights [food]
  (into
   [{:title [:i18n ::energy-highlight-title]
     :detail [:span (get-kj food)
              (when-let [kcal (:measurement/observation (:food/calories food))]
                [:div.small (wrap-in-portion-span kcal) " kcal"])]
     :href "#naeringsinnhold"
     :class "mmm-mobile"
     :aria-hidden "true"}]
   (for [[id anchor] [["Fett" "fett"] ["Protein" "energi"] ["Karbo" "karbohydrater"]]]
     (let [constituent (->> (:food/constituents food)
                            (filter (comp #{id} :nutrient/id :constituent/nutrient))
                            first)]
       {:title [:i18n ::highlight-title (nutrient/get-name (:constituent/nutrient constituent))]
        :detail [:span (wrap-in-portion-span
                        (or (some-> constituent
                                    :measurement/quantity
                                    b/num)
                            0))
                 (some->> constituent :measurement/quantity b/symbol (str " "))]
        :href (str "#" anchor)}))))

(defn prepare-nutrient-tables [{:keys [food nutrients group]}]
  (->> (concat
        [{:headers [[:i18n ::lookup (nutrient/get-name group)]
                    [:i18n ::amount]]
          :rows (for [nutrient nutrients]
                  [[:i18n ::lookup (nutrient/get-name nutrient)]
                   (get-nutrient-quantity food (:nutrient/id nutrient))])
          :classes ["mmm-nutrient-table"]}]
        (for [nutrient nutrients]
          (when-let [nutrients (food/get-nutrients food (:nutrient/id nutrient))]
            {:headers [[:i18n ::lookup (nutrient/get-name nutrient)] [:i18n ::amount]]
             :rows (for [nutrient nutrients]
                     [[:i18n ::lookup (nutrient/get-name nutrient)]
                      (get-nutrient-quantity food (:nutrient/id nutrient))])
             :classes ["mmm-nutrient-table"]})))
       (remove nil?)))

(defn render-table [{:keys [headers rows classes]}]
  [:table.mmm-table.mmm-table-zebra {:class classes}
   [:thead
    [:tr
     (for [header headers]
       [:th header])]]
   [:tbody
    (for [row rows]
      [:tr
       (for [cell row]
         [:td cell])])]])

(defn passepartout [& body]
  [:div.mmm-container.mmm-section.mmm-mobile-phn
   [:div.mmm-passepartout
    [:div.mmm-container-focused.mmm-vert-layout-m
     body]]])

(def energy-label
  [:i18n ::energy-content-title
   {:portion [:span.js-portion-label "100 g"]}])

(def energy-label-mobile
  [:i18n ::energy-content-title-mobile
   {:portion [:span.js-portion-label "100 g"]}])

(defn prepare-langual-table [codes]
  {:headers [[:i18n ::langual-code-label]
             [:i18n ::langual-description-label]]
   :rows (for [{:langual-code/keys [id description]} codes]
           [id (food/humanize-langual-classification description)])})

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
          [:article.mmm-vert-layout-spread
           [:h1.mmm-h1 food-name]
           [:div.mmm-vert-layout-s.mmm-mtm
            [:h2.mmm-p.mmm-desktop energy-label]
            [:h2.mmm-p.mmm-mobile.mmm-mbs {:aria-hidden "true"}
             energy-label-mobile]
            [:p.mmm-h3.mmm-mbs.mmm-desktop (energy food)]
            [:div.mmm-cards
             (->> (prepare-macro-highlights food)
                  (map DetailFocusCard))]]]
          [:aside
           (Toc {:title [:i18n ::toc-title]
                 :icon :fontawesome.solid/circle-info
                 :contents [{:title [:i18n ::energy-title]
                             :href "#energi"}
                            {:title [:i18n ::fat-title]
                             :href "#fett"}
                            {:title [:i18n ::carbohydrates-title]
                             :href "#karbohydrater"}
                            {:title [:i18n ::vitamins-title]
                             :href "#vitaminer"}
                            {:title [:i18n ::minerals-title]
                             :href "#mineraler"}
                            {:title [:i18n ::trace-elements-title]
                             :href "#sporstoffer"}
                            {:title [:i18n ::classification-title]
                             :href "#klassifisering"}]})]]]]
       [:div.mmm-container.mmm-section
        [:div.mmm-flex-desktop.mmm-flex-bottom.mmm-mbl
         [:h2.mmm-h2.mmm-mbn#naringsinnhold [:i18n ::nutrition-title]]
         [:div.mmm-vert-layout-s
          [:p [:i18n ::portion-size]]
          (Select
           {:id "portion-selector"
            :class "mmm-input-m"
            :options (into [[:option {:value "100"} "100 gram"]]
                           (for [portion (:food/portions food)]
                             (let [grams (int (b/num (:portion/quantity portion)))]
                               [:option {:value grams} (str "1 " (str/lower-case (:portion-kind/name (:portion/kind portion)))
                                                            " (" grams " gram)")])))})]]]

       (passepartout
        [:h3.mmm-h3#energi [:i18n ::nutrition-heading]]
        [:ul.mmm-unadorned-list
         [:li energy-label ": " (energy food)]
         [:li [:i18n ::edible-part
               {:pct (-> food :food/edible-part :measurement/percent)}]]]
        (render-table (prepare-nutrition-table food)))

       (passepartout
        [:h3.mmm-h3#karbohydrater [:i18n ::carbohydrates-title]]
        (->> (food/get-nutrient-group food "Karbo")
             prepare-nutrient-tables
             (map render-table)))

       (passepartout
        [:h3.mmm-h3#fett [:i18n ::fat-title]]
        (->> (food/get-nutrient-group food "Fett")
             prepare-nutrient-tables
             (map render-table)))

       (passepartout
        [:h3.mmm-h3#vitaminer [:i18n ::vitamins-title]]
        (->> (food/get-nested-nutrient-group food "FatSolubleVitamins")
             prepare-nutrient-tables
             (map render-table))
        (->> (food/get-flattened-nutrient-group food "WaterSolubleVitamins")
             prepare-nutrient-tables
             (map render-table)))

       (passepartout
        [:h3.mmm-h3#mineraler [:i18n ::minerals-title]]
        (->> (food/get-flattened-nutrient-group food "Minerals")
             prepare-nutrient-tables
             (map render-table)))

       (passepartout
        [:h3.mmm-h3#sporstoffer [:i18n ::trace-elements-title]]
        (->> (food/get-flattened-nutrient-group food "TraceElements")
             prepare-nutrient-tables
             (map render-table)))

       [:div.mmm-container.mmm-section
        [:div.mmm-container-focused.mmm-vert-layout-m.mmm-text
         [:h3#klassifisering [:i18n ::classification-title]]
         [:ul.mmm-unadorned-list
          [:li [:i18n ::food-id {:id (:food/id food)}]]
          (when-let [latin-name (not-empty (:food/latin-name food))]
            [:li [:i18n ::scientific-name {:name latin-name}]])]
         [:p [:i18n ::classification-intro
              {:langual-url "https://www.langual.org/"}]]
         (->> (food/get-langual-codes food)
              prepare-langual-table
              render-table)]]

       [:div.mmm-container.mmm-section
        (SiteFooter)]]]]))
