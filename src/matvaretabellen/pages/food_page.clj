(ns matvaretabellen.pages.food-page
  (:require [broch.core :as b]
            [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.components.legend :refer [Legend]]
            [matvaretabellen.components.pie-chart :refer [assoc-degrees PieChart]]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.food :as food]
            [matvaretabellen.nutrient :as nutrient]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.card :refer [DetailFocusCard]]
            [mmm.components.checkbox :refer [Checkbox]]
            [mmm.components.footer :refer [CompactSiteFooter]]
            [mmm.components.select :refer [Select]]
            [mmm.components.site-header :refer [SiteHeader]]
            [mmm.components.toc :refer [Toc]]))

(defn wrap-in-portion-span [num]
  [:span {:data-portion num} num])

(defn get-calculable-quantity [measurement]
  (when-let [q (:measurement/quantity measurement)]
    (let [n (b/num q)]
      (list
       (wrap-in-portion-span
        (if (= (Math/floor n) n)
          (int n)
          n))
       " "
       (b/symbol q)))))

(defn get-nutrient-quantity [food id]
  (or (some->> (:food/constituents food)
               (filter (comp #{id} :nutrient/id :constituent/nutrient))
               first
               get-calculable-quantity)
      "–"))

(defn get-source [food id]
  (when-let [origin (some->> (:food/constituents food)
                             (filter (comp #{id} :nutrient/id :constituent/nutrient))
                             first
                             :measurement/origin)]
    [:a.mmm-link {:href (str "#" (:origin/id origin))
                  :title [:i18n ::lookup (:origin/description origin)]}
     (:origin/id origin)]))

(defn prepare-nutrition-table [food]
  {:headers [{:text [:i18n ::nutrients]}
             {:text [:i18n ::amount]
              :class "mvt-amount"}
             {:text [:i18n ::source]
              :class "mvt-source"}]
   :rows (for [id ["Fett"
                   "Karbo"
                   "Fiber"
                   "Protein"
                   "Alko"
                   "Vann"]]
           [{:text [:i18n ::lookup
                    (some->> (:food/constituents food)
                             (filter (comp #{id} :nutrient/id :constituent/nutrient))
                             first
                             :constituent/nutrient
                             nutrient/get-name)]}
            {:text (get-nutrient-quantity food id)}
            {:text (get-source food id)
             :class "mvt-source"}])
   :classes ["mmm-nutrient-table"]})

(defn get-kj [food]
  (when (:measurement/quantity (:food/energy food))
    (get-calculable-quantity (:food/energy food))))

(defn energy [food]
  (concat
   (get-kj food)
   (when-let [kcal (:measurement/observation (:food/calories food))]
     (list " (" (wrap-in-portion-span kcal) " kcal)"))))

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
        [{:headers [{:text [:i18n ::lookup (nutrient/get-name group)]}
                    {:text [:i18n ::amount]
                     :class "mvt-amount"}
                    {:text [:i18n ::source]
                     :class "mvt-source"}]
          :rows (for [nutrient nutrients]
                  [{:text [:i18n ::lookup (nutrient/get-name nutrient)]}
                   {:text (get-nutrient-quantity food (:nutrient/id nutrient))}
                   {:text (get-source food (:nutrient/id nutrient))
                    :class "mvt-source"}])
          :classes ["mmm-nutrient-table"]}]
        (mapcat
         #(when-let [nutrients (food/get-nutrients food (:nutrient/id %))]
            (prepare-nutrient-tables
             {:food food
              :nutrients nutrients
              :group %}))
         nutrients))
       (remove nil?)))

(defn get-nutrient-rows [food nutrient & [level]]
  (let [level (or level 0)]
    (into [[{:text [:i18n ::lookup (nutrient/get-name nutrient)]
             :level level}
            {:text (get-nutrient-quantity food (:nutrient/id nutrient))}
            {:text (get-source food (:nutrient/id nutrient))
             :class "mvt-source"}]]
          (let [level (inc level)]
            (->> (:nutrient/id nutrient)
                 (food/get-nutrients food)
                 (mapcat #(get-nutrient-rows food % level)))))))

(defn prepare-nested-nutrient-table [{:keys [food nutrients group]}]
  {:headers [{:text [:i18n ::lookup (nutrient/get-name group)]}
             {:text [:i18n ::amount]
              :class "mvt-amount"}
             {:text [:i18n ::source]
              :class "mvt-source"}]
   :rows (mapcat #(get-nutrient-rows food %) nutrients)
   :classes ["mmm-nutrient-table"]})

(defn render-table [{:keys [headers rows classes]}]
  [:table.mmm-table.mmm-table-zebra {:class classes}
   [:thead
    [:tr
     (for [header headers]
       [:th (dissoc header :text) (:text header)])]]
   [:tbody
    (for [row rows]
      [:tr
       (for [cell row]
         [:td (dissoc cell :text :level)
          (cond->> (:text cell)
            (< 0 (or (:level cell) 0))
            (conj [:span {:class (case (:level cell)
                                   1 "mmm-mlm"
                                   2 "mmm-mll"
                                   "mmm-mlxl")}]))])])]])

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
  {:headers [{:text [:i18n ::langual-code-label]}
             {:text [:i18n ::langual-description-label]}]
   :rows (for [{:langual-code/keys [id description]} codes]
           [{:text id} {:text (food/humanize-langual-classification description)}])})

(defn render-sources [page sources]
  (into
   [:dl.mmm-dl]
   (mapcat
    (fn [{:origin/keys [id description]}]
      (list [:dt {:id id} id]
            [:dd (-> (get description (:page/locale page))
                     food/hyperlink-string)]))
    sources)))

(def slice-legend
  [{:nutrient-id "Fett"    :color "var(--mt-color-fat)"}
   {:nutrient-id "Karbo"   :color "var(--mt-color-carbs)"}
   {:nutrient-id "Protein" :color "var(--mt-color-protein)"}
   {:nutrient-id "Vann"    :color "var(--mt-color-water)"}
   {:nutrient-id "Fiber"   :color "var(--mt-color-fiber)"}
   {:nutrient-id "Alko"    :color "var(--mt-color-alco)"}])

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
                              [:i18n ::lookup (nutrient/get-name (:constituent/nutrient constituent))]
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
                                  [:i18n ::lookup (nutrient/get-name (:constituent/nutrient constituent))]
                                  ": "
                                  [:strong (int (* 100 (/ value total))) " %"]]})))
           (remove nil?)
           (remove #(= 0.0 (:value %)))
           (sort-by (comp - :value))))))

(defn render [context db page]
  (let [food (d/entity (:foods/db context) [:food/id (:page/food-id page)])
        locale (:page/locale page)
        food-name (get-in food [:food/name locale])]
    [:html {:class "mmm"}
     [:body
      [:script {:type "text/javascript"}
       (str "if (localStorage.getItem(\"show-sources\") != \"true\") {\n"
            "  document.body.classList.add(\"mvt-source-hide\");\n"
            "}")]
      (SiteHeader {:home-url "/"})
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
                             :href "#klassifisering"}
                            {:title [:i18n ::sources]
                             :href "#kilder"}]})]]]]
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
                              (assoc entry :label [:i18n ::lookup (nutrient/get-name (d/entity db [:nutrient/id (:nutrient-id entry)]))]))})]])

       (passepartout
        [:h3.mmm-h3#energi [:i18n ::nutrition-heading]]
        [:div.mmm-flex.mmm-flex-bottom
         [:ul.mmm-ul.mmm-unadorned-list
          [:li energy-label ": " (energy food)]
          [:li [:i18n ::edible-part
                {:pct (-> food :food/edible-part :measurement/percent)}]]]
         [:p.mmm-p.mmm-desktop
          (Checkbox {:label [:i18n ::show-sources]
                     :class :mvt-source-toggler})]]
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
        (->> (food/get-nutrient-group food "FatSolubleVitamins")
             prepare-nested-nutrient-table
             render-table)
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
         [:p.mmm-p.mmm-desktop
          (Checkbox {:label [:i18n ::show-all-sources]
                     :class :mvt-source-toggler})]
         (->> (food/get-sources food)
              (render-sources page))]]

       [:div.mmm-container.mmm-section
        (CompactSiteFooter)]]]]))
