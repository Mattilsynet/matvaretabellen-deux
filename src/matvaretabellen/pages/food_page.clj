(ns matvaretabellen.pages.food-page
  (:require [broch.core :as b]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.components.legend :refer [Legend]]
            [matvaretabellen.components.pie-chart :refer [assoc-degrees PieChart]]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.food :as food]
            [matvaretabellen.nutrient :as nutrient]
            [matvaretabellen.rda :as rda]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.button :refer [Button]]
            [mmm.components.card :refer [DetailFocusCard]]
            [mmm.components.checkbox :refer [Checkbox]]
            [mmm.components.footer :refer [CompactSiteFooter]]
            [mmm.components.select :refer [Select]]
            [mmm.components.site-header :refer [SiteHeader]]
            [mmm.components.toc :refer [Toc]]))

(defn wrap-in-portion-span [num & [{:keys [decimals class]}]]
  [:span (cond-> {:data-portion num}
           decimals (assoc :data-decimals (str decimals))
           class (assoc :class class))
   num])

(defn get-calculable-quantity [measurement & [opt]]
  (when-let [q (:measurement/quantity measurement)]
    (let [n (b/num q)
          decimals (if (= (Math/floor n) n)
                     0
                     (or (:decimals opt) 1))]
      (list
       (wrap-in-portion-span
        (format (str "%." decimals "f") n)
        opt)
       " "
       [:span.mvt-sym (b/symbol q)]))))

(defn get-nutrient-quantity [food nutrient-id]
  (or (some->> (food/get-nutrient-measurement food nutrient-id)
               get-calculable-quantity)
      "–"))

(defn get-nutrient-link [db locale nutrient]
  (let [label [:i18n :i18n/lookup (nutrient/get-name nutrient)]
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

(defn prepare-nutrition-table [db locale food]
  {:headers [{:text [:i18n ::nutrients]}
             {:text [:i18n ::amount]
              :class "mvt-amount"}
             {:text [:i18n ::source]
              :class "mvt-source"}]
   :rows (for [id nutrition-table-row-ids]
           (let [nutrient (:constituent/nutrient (food/get-nutrient-measurement food id))]
             [{:text (get-nutrient-link db locale nutrient)}
              {:text (get-nutrient-quantity food id)}
              {:text (get-source food id)
               :class "mvt-source"}]))})

(defn get-kj [food & [opt]]
  (when (:measurement/quantity (:food/energy food))
    (get-calculable-quantity (:food/energy food) (assoc opt :decimals 0))))

(defn get-kcal [food & [opt]]
  (when-let [kcal (:measurement/observation (:food/calories food))]
    (list (wrap-in-portion-span kcal (assoc opt :decimals 0)) " kcal")))

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
       {:title [:i18n ::highlight-title (nutrient/get-name (:constituent/nutrient constituent))]
        :detail [:span (wrap-in-portion-span
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
    (let [recommendation (->> (:constituent/nutrient measurement)
                              :nutrient/id
                              recommendations)]
      (cond
        (:rda.recommendation/max-amount recommendation)
        (pct (b// q (:rda.recommendation/max-amount recommendation)))

        (:rda.recommendation/min-amount recommendation)
        (pct (b// q (:rda.recommendation/min-amount recommendation)))

        (:rda.recommendation/average-amount recommendation)
        (pct (b// q (:rda.recommendation/average-amount recommendation)))

        ;; Min/max/average energy percent not yet supported
        ))))

(defn prepare-nutrient-tables [db locale {:keys [food recommendations nutrients group]}]
  (->> (concat
        [{:headers (->> [{:text (get-nutrient-link db locale group)}
                         {:text [:i18n ::amount]
                          :class "mvt-amount"}
                         (when recommendations
                           {:text [:abbr.mmm-abbr {:title [:i18n ::rda-explanation]}
                                   [:i18n ::rda-pct]]
                            :class "mmm-td-min"})
                         {:text [:i18n ::source]
                          :class "mvt-source"}]
                        (remove nil?))
          :rows (for [nutrient nutrients]
                  (->> [{:text (get-nutrient-link db locale nutrient)}
                        {:text (get-nutrient-quantity food (:nutrient/id nutrient))}
                        (when recommendations
                          {:text (->> (food/get-nutrient-measurement food (:nutrient/id nutrient))
                                      (get-recommended-daily-allowance recommendations))
                           :class "mmm-tac"})
                        {:text (get-source food (:nutrient/id nutrient))
                         :class "mvt-source"}]
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
                 {:text (get-nutrient-quantity food (:nutrient/id nutrient))}
                 (when recommendations
                   {:text (->> (food/get-nutrient-measurement food (:nutrient/id nutrient))
                               (get-recommended-daily-allowance recommendations))
                    :class "mmm-tac"})
                 {:text (get-source food (:nutrient/id nutrient))
                  :class "mvt-source"}]
                (remove nil?))]
          (let [level (inc level)]
            (->> (:nutrient/id nutrient)
                 (food/get-nutrients food)
                 (mapcat #(get-nutrient-rows food % recommendations db locale level)))))))

(defn prepare-nested-nutrient-table [db locale {:keys [food nutrients group recommendations]}]
  {:headers (->> [{:text (get-nutrient-link db locale group)}
                  {:text [:i18n ::amount]
                   :class "mvt-amount"}
                  (when recommendations
                    {:text [:abbr.mmm-abbr {:title [:i18n ::rda-explanation]}
                            [:i18n ::rda-pct]]
                     :class "mmm-td-min"})
                  {:text [:i18n ::source]
                   :class "mvt-source"}]
                 (remove nil?))
   :rows (mapcat #(get-nutrient-rows food % recommendations db locale) nutrients)})

(defn render-table [{:keys [headers rows classes]}]
  [:table.mmm-table.mmm-table-zebra {:class classes}
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
                              [:i18n :i18n/lookup (nutrient/get-name (:constituent/nutrient constituent))]
                              ": "
                              [:strong
                               (wrap-in-portion-span value)
                               (some->> constituent :measurement/quantity b/symbol (str " "))]]})))
       (remove nil?)
       (remove #(= 0.0 (:value %)))
       (sort-by (comp - :value))))

(defn prepare-percent-slices [food ids]
  (let [constituents (filter (comp ids :nutrient/id :constituent/nutrient) (:food/constituents food))
        total (apply + (keep #(some-> % :measurement/quantity b/num) constituents))]
    (when (< 0 total)
      (->> (for [constituent constituents]
             (let [id (:nutrient/id (:constituent/nutrient constituent))
                   value (some-> constituent :measurement/quantity b/num)]
               (when value
                 {:id (str id (hash ids))
                  :value value
                  :color (nutrient-id->color id)
                  :hover-content [:span
                                  [:i18n :i18n/lookup (nutrient/get-name (:constituent/nutrient constituent))]
                                  ": "
                                  [:strong (int (* 100 (/ value total))) " %"]]})))
           (remove nil?)
           (remove #(= 0.0 (:value %)))
           (sort-by (comp - :value))))))

(defn passepartout [& body]
  [:div.mmm-container.mmm-section.mmm-mobile-phn
   [:div.mmm-passepartout
    [:div.mmm-container-focused.mmm-vert-layout-m
     body]]])

(def source-toggle
  [:p.mmm-p.mmm-desktop
   (Checkbox {:label [:i18n ::show-sources]
              :class :mvt-source-toggler})])

(defn passepartout-title [id title]
  [:div.mmm-flex.mmm-flex-bottom
   [:h3.mmm-h3 {:id id} title]
   source-toggle])

(defn render-rda-select [db selected]
  (let [profiles (rda/get-profiles-per-demographic db)]
    [:div.mmm-vert-layout-s
     [:p [:i18n ::rda-select-label]]
     (Select
      {:id "rda-selector"
       :class "mmm-input-m"
       :options (for [profile profiles]
                  [:option (cond-> {:value (:rda/id profile)}
                             (= (:rda/id selected) (:rda/id profile))
                             (assoc :selected "true"))
                   [:i18n :i18n/lookup (:rda/demographic profile)]])})]))

(defn render-portion-select [locale portions]
  [:div.mmm-vert-layout-s
   [:p [:i18n ::portion-size]]
   (Select
    {:id "portion-selector"
     :class "mmm-input-m"
     :options (into [[:option {:value "100"} [:i18n ::grams {:value 100}]]]
                    (for [portion portions]
                      (let [grams (int (b/num (:portion/quantity portion)))]
                        [:option {:value grams}
                         [:i18n ::portion-with-grams
                          {:portion (str "1 " (str/lower-case
                                               (get-in portion [:portion/kind :portion-kind/name locale])))
                           :grams grams}]])))})])

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

(defn render-toc [contents]
  [:aside
   (Toc
    {:title [:i18n ::toc-title]
     :icon :fontawesome.solid/circle-info
     :contents contents})])

(defn render [context db page]
  (let [food (d/entity (:foods/db context) [:food/id (:page/food-id page)])
        locale (:page/locale page)
        food-name (get-in food [:food/name locale])
        rda-profile (d/entity (:app/db context) [:rda/id "rda-1045046097"])
        recommendations (->> rda-profile
                             :rda/recommendations
                             (map (juxt :rda.recommendation/nutrient-id identity))
                             (into {}))]
    [:html {:class "mmm"}
     [:body
      [:script {:type "text/javascript"}
       (str "if (localStorage.getItem(\"show-sources\") != \"true\") {\n"
            "  document.body.classList.add(\"mvt-source-hide\");\n"
            "}")]
      (SiteHeader {:home-url "/"
                   :extra-link {:text [:i18n :i18n/other-language]
                                :url (urls/get-food-url
                                      ({:en :nb :nb :en} locale) food)}})
      [:div
       [:div.mmm-themed.mmm-brand-theme1
        [:div.mmm-container.mmm-section
         (Breadcrumbs
          {:links (crumbs/crumble locale
                                  (:food/food-group food)
                                  {:text food-name})})]
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
              [:p.mmm-h3.mmm-mbs.mmm-desktop (energy food)]]
             [:div.mmm-desktop
              (Button {:class [:mmm-hidden :mvt-compare-food]
                       :text [:i18n ::compare-food]
                       :inline? true
                       :secondary? true
                       :icon :fontawesome.solid/code-compare})]]
            [:div.mmm-cards
             (->> (prepare-macro-highlights food)
                  (map DetailFocusCard))]]]
          (render-toc (get-toc-items))]]]
       [:div.mmm-container.mmm-section
        [:div.mmm-flex-desktop.mmm-flex-bottom.mmm-mbl
         [:h2.mmm-h2.mmm-mbn#naringsinnhold [:i18n ::nutrition-title]]
         [:div.mmm-flex.mmm-flex-gap
          (render-rda-select (:app/db context) rda-profile)
          (render-portion-select locale (:food/portions food))]]]

       (passepartout
        [:div.mmm-flex-gap-huge.mvt-cols-2-1-labeled
         [:div.col-2
          [:div.label [:h3.mmm-h3 [:i18n ::composition]]]
          (PieChart {:slices (assoc-degrees 70 (prepare-value-slices food #{"Fett" "Karbo" "Protein" "Vann" "Fiber" "Alko"}))
                     :hoverable? true})]
         [:div.col-2
          [:div.label [:h3.mmm-h3 [:i18n ::energy-content]]]
          (PieChart {:slices (assoc-degrees 30 (prepare-percent-slices food #{"Fett" "Karbo" "Protein"}))
                     :hoverable? true})]
         [:div.col-1
          (Legend {:entries (for [entry slice-legend]
                              (assoc entry :label [:i18n :i18n/lookup
                                                   (nutrient/get-name (d/entity db [:nutrient/id (:nutrient-id entry)]))]))})]])

       (passepartout
        [:h3.mmm-h3#energi [:i18n ::nutrition-heading]]
        [:div.mmm-flex.mmm-flex-bottom
         [:ul.mmm-ul.mmm-unadorned-list
          [:li energy-label ": " (energy food)]
          [:li [:i18n ::edible-part
                {:pct (-> food :food/edible-part :measurement/percent)}]]]
         source-toggle]
        (render-table (prepare-nutrition-table (:app/db context) locale food)))

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

       (passepartout
        (passepartout-title "mineraler" [:i18n ::minerals-title])
        (->> (assoc (food/get-flattened-nutrient-group food "Minerals")
                    :recommendations recommendations)
             (prepare-nutrient-tables (:app/db context) locale)
             (map render-table)))

       (passepartout
        (passepartout-title "sporstoffer" [:i18n ::trace-elements-title])
        (->> (assoc (food/get-flattened-nutrient-group food "TraceElements")
                    :recommendations recommendations)
             (prepare-nutrient-tables (:app/db context) locale)
             (map render-table)))

       [:div.mmm-container.mmm-section-spaced
        [:div.mmm-container-focused.mmm-vert-layout-m.mmm-text.mmm-mobile-phn
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
        [:div.mmm-container-focused.mmm-vert-layout-m.mmm-text.mmm-mobile-phn
         [:h3#kilder [:i18n ::sources]]
         (->> (food/get-sources food)
              (render-sources page))]]

       [:div.mmm-container.mmm-section
        (CompactSiteFooter)]

       [:script {:type "text/json" :id "data"}
        (json/write-str
         {:id (:food/id food)
          :url (urls/get-food-url locale food)
          :foodName (get (:food/name food) locale)
          :energyKj (b/num (:measurement/quantity (:food/energy food)))
          :energyKcal (:measurement/observation (:food/calories food))
          :ediblePart (:measurement/percent (:food/edible-part food))
          :constituents (->> (for [constituent (:food/constituents food)]
                               [(-> constituent :constituent/nutrient :nutrient/id)
                                {:quantity [(or (some-> constituent :measurement/quantity b/num) 0)
                                            (or (some-> constituent :measurement/quantity b/symbol) "g")]}])
                             (into {}))})]]]]))
