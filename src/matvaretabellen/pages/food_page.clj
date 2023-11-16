(ns matvaretabellen.pages.food-page
  (:require [broch.core :as b]
            [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.components.comparison :as comparison]
            [matvaretabellen.components.legend :refer [Legend]]
            [matvaretabellen.components.pie-chart :refer [assoc-degrees PieChart]]
            [matvaretabellen.food :as food]
            [matvaretabellen.food-name :as food-name]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.nutrient :as nutrient]
            [matvaretabellen.rda :as rda]
            [matvaretabellen.urls :as urls]
            [mmm.components.button :refer [Button]]
            [mmm.components.card :refer [DetailFocusCard]]
            [mmm.components.checkbox :refer [Checkbox]]
            [mmm.components.select :refer [Select]]
            [mmm.components.site-header :refer [SiteHeader]]
            [mmm.components.tabs :refer [Tabs]]
            [mmm.components.toc :refer [Toc]]))

(defn get-nutrient-link [db locale nutrient]
  (let [label [:i18n :i18n/lookup (:nutrient/name nutrient)]
        url (urls/get-nutrient-url locale nutrient)]
    (if (d/entity db [:page/uri url])
      [:a.mmm-link {:href (urls/get-nutrient-url locale nutrient)} label]
      label)))

(defn get-source [food id]
  (when-let [origin (some->> (:food/constituents food)
                             (filter (comp #{id} :nutrient/id :constituent/nutrient))
                             first
                             :measurement/origin)]
    [:a.mmm-link {:href (str "#" (:origin/id origin))
                  :title [:i18n :i18n/lookup (:origin/description origin)]}
     (:origin/id origin)]))

(def nutrition-table-row-ids
  ["Fett" "Karbo" "Fiber" "Protein" "Alko" "Vann"])

(def grams-to-kj-factor
  {"Fett" 37
   "Karbo" 17
   "Protein" 17
   "Vann" 0
   "Fiber" 8
   "Alko" 29})

(defn get-constituent-energy [constituent]
  (some-> constituent :measurement/quantity b/num
          (* (grams-to-kj-factor (-> constituent :constituent/nutrient :nutrient/id)))))

(defn prepare-nutrition-table [db locale food]
  (let [constituents (filter (comp (set nutrition-table-row-ids)
                                   :nutrient/id
                                   :constituent/nutrient)
                             (:food/constituents food))
        total (apply + (keep get-constituent-energy constituents))]
    {:headers [{:text [:i18n ::nutrients]}
               {:text [:i18n ::source]
                :class "mvt-source"}
               {:text [:i18n ::amount]
                :class "mvt-amount mmm-tar"}
               {:text "Energi%"
                :class "mvt-amount mmm-tar"}]
     :rows (for [id nutrition-table-row-ids]
             (let [constituent (food/get-nutrient-measurement food id)
                   nutrient (:constituent/nutrient constituent)
                   value (or (get-constituent-energy constituent) 0)]
               [{:text (get-nutrient-link db locale nutrient)}
                {:text (get-source food id)
                 :class "mvt-source"}
                {:text (food/get-nutrient-quantity food id)
                 :class "mmm-tar mvt-amount"}
                {:text (str (int (* 100 (/ value total))) "%")
                 :class "mmm-tar mvt-amount"}]))}))

(defn get-kj [food & [opt]]
  (when (:measurement/quantity (:food/energy food))
    (food/get-calculable-quantity (:food/energy food) (assoc opt :decimals 0))))

(defn get-kcal [food & [opt]]
  (when-let [kcal (some-> food :food/calories :measurement/observation parse-long)]
    (list (food/wrap-in-portion-span kcal (assoc opt :decimals 0)) " kcal")))

(defn energy [food]
  (concat
   (get-kj food {:class "mvt-kj"})
   (when-let [formatted-kcal (get-kcal food {:class "mvt-kcal"})]
     (concat [" ("] formatted-kcal [")"]))))

(defn prepare-macro-highlights [food]
  (into
   [{:title [:i18n ::energy-highlight-title]
     :detail [:span (get-kj food)
              (when-let [formatted-kcal (get-kcal food)]
                [:div.small formatted-kcal])]
     :href "#naeringsinnhold"
     :class "mmm-mobile"
     :aria-hidden "true"}]
   (for [[id anchor] [["Fett" "fett"] ["Protein" "energi"] ["Karbo" "karbohydrater"]]]
     (let [constituent (->> (:food/constituents food)
                            (filter (comp #{id} :nutrient/id :constituent/nutrient))
                            first)]
       {:title [:i18n ::highlight-title (:nutrient/name (:constituent/nutrient constituent))]
        :detail [:span (food/wrap-in-portion-span
                        (or (some-> constituent
                                    :measurement/quantity
                                    b/num)
                            0))
                 (some->> constituent :measurement/quantity b/symbol (str " "))]
        :href (str "#" anchor)}))))

(defn pct [n]
  (str (int (* 100 n)) "&nbsp;%"))

(defn get-recommended-daily-allowance [recommendations measurement]
  (when-let [q (:measurement/quantity measurement)]
    (let [nutrient-id (->> (:constituent/nutrient measurement)
                           :nutrient/id)
          recommendation (recommendations nutrient-id)]
      (when-let [v (or (:rda.recommendation/max-amount recommendation)
                       (:rda.recommendation/min-amount recommendation)
                       (:rda.recommendation/average-amount recommendation)
                       ;; Min/max/average energy percent not yet supported
                       )]
        [:span.mvt-rda
         {:data-nutrient-id nutrient-id}
         (pct (b// q v))]))))

(defn prepare-nutrient-tables [db locale {:keys [food recommendations nutrients group]}]
  (->> (concat
        [{:headers (->> [{:text (get-nutrient-link db locale group)}
                         {:text [:i18n ::source]
                          :class "mvt-source"}
                         {:text [:i18n ::amount]
                          :class "mvt-amount mmm-tar"}
                         (when recommendations
                           {:text [:abbr.mmm-abbr {:title [:i18n ::rda-explanation]}
                                   [:i18n ::rda-pct]]
                            :class "mmm-td-min mmm-tar"})]
                        (remove nil?))
          :rows (for [nutrient nutrients]
                  (->> [{:text (get-nutrient-link db locale nutrient)}
                        {:text (get-source food (:nutrient/id nutrient))
                         :class "mvt-source"}
                        {:text (food/get-nutrient-quantity food (:nutrient/id nutrient))
                         :class "mmm-tar mvt-amount"}
                        (when recommendations
                          {:text (->> (food/get-nutrient-measurement food (:nutrient/id nutrient))
                                      (get-recommended-daily-allowance recommendations))
                           :class "mmm-tar"})]
                       (remove nil?)))}]
        (mapcat
         #(when-let [nutrients (food/get-nutrients food (:nutrient/id %))]
            (prepare-nutrient-tables
             db
             locale
             {:food food
              :nutrients nutrients
              :recommendations recommendations
              :group %}))
         nutrients))
       (remove nil?)))

(defn get-nutrient-rows [food nutrient recommendations db locale & [level]]
  (let [level (or level 0)]
    (into [(->> [{:text (get-nutrient-link db locale nutrient)
                  :level level}
                 {:text (get-source food (:nutrient/id nutrient))
                  :class "mvt-source"}
                 {:text (food/get-nutrient-quantity food (:nutrient/id nutrient))
                  :class "mmm-tar mvt-amount"}
                 (when recommendations
                   {:text (->> (food/get-nutrient-measurement food (:nutrient/id nutrient))
                               (get-recommended-daily-allowance recommendations))
                    :class "mmm-tar"})]
                (remove nil?))]
          (let [level (inc level)]
            (->> (:nutrient/id nutrient)
                 (food/get-nutrients food)
                 (mapcat #(get-nutrient-rows food % recommendations db locale level)))))))

(defn prepare-nested-nutrient-table [db locale {:keys [food nutrients group recommendations]}]
  {:headers (->> [{:text (get-nutrient-link db locale group)}
                  {:text [:i18n ::source]
                   :class "mvt-source"}
                  {:text [:i18n ::amount]
                   :class "mvt-amount mmm-tar"}
                  (when recommendations
                    {:text [:abbr.mmm-abbr {:title [:i18n ::rda-explanation]}
                            [:i18n ::rda-pct]]
                     :class "mmm-td-min mmm-tar"})]
                 (remove nil?))
   :rows (mapcat #(get-nutrient-rows food % recommendations db locale) nutrients)})

(defn render-table [{:keys [headers rows classes id]}]
  [:table.mmm-table.mmm-table-zebra {:class classes :id id}
   [:thead
    (let [row (if (map? headers) headers {:cols headers})]
      [:tr (dissoc row :cols)
       (for [header (:cols row)]
         [:th (dissoc header :text) (:text header)])])]
   [:tbody
    (for [row rows]
      (let [row (if (map? row) row {:cols row})]
        [:tr (dissoc row :cols)
         (for [cell (:cols row)]
           [(or (:tag cell) :td) (dissoc cell :text :level :tag)
            (cond->> (:text cell)
              (< 0 (or (:level cell) 0))
              (conj [:span {:class (case (:level cell)
                                     1 "mmm-mlm"
                                     2 "mmm-mll"
                                     "mmm-mlxl")}]))])]))]])

(def energy-label
  [:i18n ::energy-content-title
   {:portion [:span.js-portion-label "100 g"]}])

(def kcal-label
  [:i18n ::kcal-content-title
   {:portion [:span.js-portion-label "100 g"]}])

(def energy-label-mobile
  [:i18n ::energy-content-title-mobile
   {:portion [:span.js-portion-label "100 g"]}])

(defn prepare-langual-table [codes]
  {:headers [{:text [:i18n ::langual-code-label]}
             {:text [:i18n ::langual-description-label]}]
   :rows (for [{:langual-code/keys [id description]} codes]
           [{:text id} {:text (food/humanize-langual-classification description)}])})

(defn render-sources [page sources]
  [:dl.mmm-dl
   (map
    (fn [{:origin/keys [id description]}]
      [:div
       [:div.mmm-focus {:id id}
        [:dt id]
        [:dd (-> (get description (:page/locale page))
                 food/hyperlink-string)]]])
    sources)])

(def slice-legend
  [{:nutrient-id "Fett"    :color "var(--mt-color-fat)"}
   {:nutrient-id "Karbo"   :color "var(--mt-color-carbs)"}
   {:nutrient-id "Fiber"   :color "var(--mt-color-fiber)"}
   {:nutrient-id "Protein" :color "var(--mt-color-protein)"}
   {:nutrient-id "Alko"    :color "var(--mt-color-alco)"}
   {:nutrient-id "Vann"    :color "var(--mt-color-water)"}])

(def nutrient-id->color
  (into {} (map (juxt :nutrient-id :color) slice-legend)))

(defn prepare-value-slices [food ids]
  (->> (for [id ids]
         (let [constituent (->> (:food/constituents food)
                                (filter (comp #{id} :nutrient/id :constituent/nutrient))
                                first)
               value (some-> constituent :measurement/quantity b/num)]
           (when value
             {:id (str id (hash ids))
              :value value
              :color (nutrient-id->color id)
              :hover-content [:span
                              [:i18n :i18n/lookup (:nutrient/name (:constituent/nutrient constituent))]
                              ": "
                              [:strong
                               (food/wrap-in-portion-span value)
                               (some->> constituent :measurement/quantity b/symbol (str " "))]]})))
       (remove nil?)
       (remove #(= 0.0 (:value %)))
       (sort-by (comp - :value))))

(defn prepare-energy-content-slices [food ids]
  (let [constituents (filter (comp ids :nutrient/id :constituent/nutrient) (:food/constituents food))
        total (apply + (keep get-constituent-energy constituents))]
    (when (< 0 total)
      (->> (for [constituent constituents]
             (let [id (:nutrient/id (:constituent/nutrient constituent))
                   value (get-constituent-energy constituent)]
               (when value
                 {:id (str id (hash ids))
                  :value value
                  :color (nutrient-id->color id)
                  :hover-content [:span
                                  [:i18n :i18n/lookup (:nutrient/name (:constituent/nutrient constituent))]
                                  ": "
                                  [:strong (int value) " kJ (" (int (* 100 (/ value total))) " %)"]]})))
           (remove nil?)
           (remove #(= 0.0 (:value %)))
           (sort-by (comp - :value))))))

(defn passepartout [& body]
  [:div.mmm-container.mmm-section.mmm-mobile-phn
   [:div.mmm-passepartout
    [:div.mmm-container-medium.mmm-vert-layout-m
     body]]])

(def source-toggle
  [:p.mmm-p.mmm-desktop
   (Checkbox {:label [:i18n ::show-sources]
              :class :mvt-source-toggler})])

(defn passepartout-title [id title & rest]
  [:div.mmm-flex.mmm-flex-bottom
   [:h3.mmm-h3 {:id id} title]
   source-toggle
   rest])

(defn render-rda-select [db selected]
  (let [profiles (rda/get-profiles-per-demographic db)]
    [:div.mmm-container-medium.mmm-section.mmm-flex.mmm-flex-jr
     [:div.mmm-vert-layout-s.mmm-alr
      [:p [:i18n ::rda-select-label]]
      (Select
       {:size :m
        :class [:mmm-input-m :mvt-rda-selector]
        :options (for [profile profiles]
                   [:option (cond-> {:value (:rda/id profile)}
                              (= (:rda/id selected) (:rda/id profile))
                              (assoc :selected "true"))
                    [:i18n :i18n/lookup (:rda/demographic profile)]])})]]))

(defn render-portion-select [locale portions]
  [:div.mmm-vert-layout-s
   [:p [:i18n ::portion-size]]
   (Select
    {:id "portion-selector"
     :class "mmm-input-m"
     :options (into [[:option {:value "100"} [:i18n ::select-grams {:value 100}]]]
                    (for [portion portions]
                      (let [grams (b/num (:portion/quantity portion))]
                        [:option {:value grams}
                         [:i18n ::select-portion-with-grams
                          {:portion (str "1 " (str/lower-case
                                               (get-in portion [:portion/kind :portion-kind/name locale])))
                           :grams [:i18n :i18n/number {:n grams}]}]])))})])

(defn get-toc-items []
  [{:title [:i18n ::energy-title]
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
    :href "#klassifisering"}
   {:title [:i18n ::sources]
    :href "#kilder"}])

(defn render-toc [{:keys [contents class]}]
  [:aside.mvt-aside-col
   (Toc
    {:title [:i18n ::toc-title]
     :icon :fontawesome.solid/circle-info
     :contents contents
     :class class})])

(defn render-compare-button [food opts]
  (Button (merge {:class [:mmm-hidden :mvt-compare-food]
                  :text [:i18n ::compare-food]
                  :secondary? true
                  :icon :fontawesome.solid/code-compare
                  :data-food-id (:food/id food)
                  :data-food-name [:i18n :i18n/lookup (:food/name food)]}
                 opts)))

(defn summarize-constituent [food id locale]
  (let [c (food/get-nutrient-measurement food id)]
    (str (int (b/num (:measurement/quantity c)))
         (b/symbol (:measurement/quantity c)) " "
         (str/lower-case (get-in c [:constituent/nutrient :nutrient/name locale])))))

(defn get-open-graph-description [food locale]
  [:meta
   {:property "og:description"
    :content [:i18n ::open-graph-description
              {:food-name (get-in food [:food/name locale])
               :energy (->> [(some-> (:food/energy food)
                                     :measurement/quantity
                                     b/num
                                     int
                                     (str " kJ"))
                             (some-> (:food/calories food)
                                     :measurement/observation
                                     parse-long
                                     (str " kcal"))]
                            (remove nil?)
                            (str/join " / "))
               :macros (->> ["Fett" "Karbo" "Protein"]
                            (map #(summarize-constituent food % locale)))}]}])

(defn render [context db page]
  (let [food (d/entity (:foods/db context) [:food/id (:page/food-id page)])
        locale (:page/locale page)
        food-name (get-in food [:food/name locale])
        rda-profile (d/entity (:app/db context) [:rda/id "rda-258206827"])
        recommendations (->> rda-profile
                             :rda/recommendations
                             (map (juxt :rda.recommendation/nutrient-id identity))
                             (into {}))]
    (layout/layout
     context
     [:head
      [:title food-name]
      (get-open-graph-description food locale)]
     [:body
      [:script {:type "text/javascript"}
       (str "if (localStorage.getItem(\"show-sources\") != \"true\") {\n"
            "  document.body.classList.add(\"mvt-source-hide\");\n"
            "}")]
      (SiteHeader {:home-url (urls/get-base-url locale)
                   :extra-link {:text [:i18n :i18n/other-language]
                                :url (urls/get-food-url
                                      ({:en :nb :nb :en} locale) food)}})
      [:div.mmm-themed.mmm-brand-theme1
       (layout/render-toolbar
        {:locale locale
         :crumbs [(:food/food-group food)
                  {:text (->> (get-in food [:food/name locale])
                              food-name/shorten-name)}]})
       [:div.mmm-container.mmm-section
        [:div.mmm-media-d.mmm-media-at
         [:article.mmm-vert-layout-spread
          [:h1.mmm-h1 food-name]
          [:div.mmm-mtm.mmm-vert-layout-s
           [:div.mmm-flex
            [:div.mmm-vert-layout-s
             [:h2.mmm-p.mmm-desktop energy-label]
             [:h2.mmm-p.mmm-mobile.mmm-mbs {:aria-hidden "true"}
              energy-label-mobile]
             [:p.mmm-h3.mmm-mbs.mmm-desktop (energy food)]]]
           [:div.mmm-cards
            (->> (prepare-macro-highlights food)
                 (map DetailFocusCard))]]
          [:div.mmm-mobile.mmm-mtm (render-compare-button food {:inline? false})]]
         (render-toc {:contents (get-toc-items)})]]]
      [:div.mmm-container.mmm-section
       [:div.mmm-flex-desktop.mmm-flex-bottom.mtv-food-page-top-section
        [:h2.mmm-h2.mtv-food-page-top-section-h2#naringsinnhold [:i18n ::nutrition-title]]
        [:div.mmm-flex.mmm-flex-bottom.mmm-flex-gap
         [:div.mmm-desktop (render-compare-button food {:inline? true})]
         (render-portion-select locale (:food/portions food))]]]

      [:div.mmm-container.mmm-section.mmm-mobile-phn
       [:div.mmm-flex.mmm-mlm
        (Tabs
         {:tabs [{:text [:i18n ::tab-diagram] :selected? true :id "piechart-segment"}
                 {:text [:i18n ::tab-table] :id "table-segment"}]})]
       [:div.mmm-passepartout {:id "piechart-display"}
        [:div.mmm-container-focused.mmm-vert-layout-m
         [:div.mvt-cols-2-1-labeled
          [:div.col-2
           [:div.label [:h3.mmm-h3 [:i18n ::composition]]]
           (PieChart {:slices (assoc-degrees 70 (prepare-value-slices food #{"Fett" "Karbo" "Protein" "Vann" "Fiber" "Alko"}))
                      :hoverable? true})]
          [:div.col-2
           [:div.label [:h3.mmm-h3 [:i18n ::energy-content]]]
           (PieChart {:slices (assoc-degrees 30 (prepare-energy-content-slices food #{"Fett" "Karbo" "Protein" "Fiber" "Alko"}))
                      :hoverable? true})]
          [:div.col-1
           (Legend {:entries (for [entry slice-legend]
                               (assoc entry :label [:i18n :i18n/lookup
                                                    (:nutrient/name (d/entity db [:nutrient/id (:nutrient-id entry)]))]))})]]]]
       [:div.mmm-passepartout.mmm-hidden {:id "table-display"}
        [:div.mmm-container-medium.mmm-vert-layout-m
         [:h3.mmm-h3#energi [:i18n ::nutrition-heading]]
         [:div.mmm-flex.mmm-flex-bottom
          [:ul.mmm-ul.mmm-unadorned-list
           [:li energy-label ": " (energy food)]
           [:li [:i18n ::edible-part
                 {:pct (-> food :food/edible-part :measurement/percent)}]]]
          source-toggle]
         (render-table (prepare-nutrition-table (:app/db context) locale food))]]]

      (passepartout
       (passepartout-title "karbohydrater" [:i18n ::carbohydrates-title])
       (->> (food/get-nutrient-group food "Karbo")
            (prepare-nutrient-tables (:app/db context) locale)
            (map render-table)))

      (passepartout
       (passepartout-title "fett" [:i18n ::fat-title])
       (->> (food/get-nutrient-group food "Fett")
            (prepare-nutrient-tables (:app/db context) locale)
            (map render-table)))

      (render-rda-select (:app/db context) rda-profile)

      (passepartout
       (passepartout-title "vitaminer" [:i18n ::vitamins-title])
       (->> (assoc (food/get-nutrient-group food "FatSolubleVitamins")
                   :recommendations recommendations)
            (prepare-nested-nutrient-table (:app/db context) locale)
            render-table)
       (->> (assoc (food/get-flattened-nutrient-group food "WaterSolubleVitamins")
                   :recommendations recommendations)
            (prepare-nutrient-tables (:app/db context) locale)
            (map render-table)))

      (render-rda-select (:app/db context) rda-profile)

      (passepartout
       (passepartout-title "mineraler-sporstoffer" [:i18n ::minerals-trace-elements-title])
       (->> (assoc (food/get-flattened-nutrient-group food "Minerals")
                   :recommendations recommendations)
            (prepare-nutrient-tables (:app/db context) locale)
            (map #(render-table (assoc % :id "mineraler"))))
       (->> (assoc (food/get-flattened-nutrient-group food "TraceElements")
                   :recommendations recommendations)
            (prepare-nutrient-tables (:app/db context) locale)
            (map #(render-table (assoc % :id "sporstoffer")))))

      [:div.mmm-container.mmm-section-spaced
       [:div.mmm-container-medium.mmm-vert-layout-m.mmm-text.mmm-mobile-phn
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

      [:div.mmm-container.mmm-section-spaced
       [:div.mmm-container-medium.mmm-vert-layout-m.mmm-text.mmm-mobile-phn
        [:h3#kilder [:i18n ::sources]]
        (->> (food/get-sources food)
             (render-sources page))]]

      [:script#food-data
       {:type "text/plain"
        :data-food-id (:food/id food)
        :data-food-name [:i18n :i18n/lookup (:food/name food)]}]

      (comparison/render-comparison-drawer locale)])))
