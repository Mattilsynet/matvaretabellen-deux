(ns matvaretabellen.pages.food-page
  (:require [broch.core :as b]
            [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [mattilsynet.design :as mtds]
            [matvaretabellen.food :as food]
            [matvaretabellen.food-name :as food-name]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.rda :as rda]
            [matvaretabellen.ui.comparison :as comparison]
            [matvaretabellen.ui.toc :refer [Toc]]
            [matvaretabellen.urls :as urls]
            [phosphor.icons :as icons]))

(defn has-popover? [constituent]
  (or (:measurement/value-type constituent)
      (:measurement/acquisition-type constituent)
      (:measurement/method-type constituent)
      (:measurement/method-indicator constituent)))

(defn ^{:indent 2} with-source-popover [food nutrient content]
  (let [constituent (food/get-nutrient-measurement food (:nutrient/id nutrient))]
    (if (has-popover? constituent)
      [:button {:popoverTarget (:nutrient/id nutrient)
                :data-popover "inline"}
       content]
      content)))

(defn get-nutrient-link [db locale nutrient]
  (try
    (let [label [:i18n :i18n/lookup (:nutrient/name nutrient)]
          url (urls/get-nutrient-url locale nutrient)]
      (if (d/entity db [:page/uri url])
        [:a {:href (urls/get-nutrient-url locale nutrient)} label]
        label))
    (catch Exception _
      (throw (ex-info "Can't get me no nutrient link"
                      {:locale locale
                       :nutrient (some->> nutrient (into {}))})))))

(defn get-source [food id]
  (when-let [source (some->> (:food/constituents food)
                             (filter (comp #{id} :nutrient/id :constituent/nutrient))
                             first
                             :measurement/source)]
    [:button {:class "mvt-source-popover"
              :popoverTarget (str "source-" (:source/id source))
              :data-popover "inline"}
     (:source/id source)]))

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
                :class "mvt-source"
                :style {:width "var(--mtds-30)"}}
               {:text [:i18n ::amount]
                :class "mvt-amount"
                :style {:width "var(--mtds-30)"}}
               {:text "Energiprosent"
                :class "mvt-amount"
                :style {:width "var(--mtds-30)"}}]
     :rows (for [id nutrition-table-row-ids]
             (let [constituent (food/get-nutrient-measurement food id)
                   nutrient (:constituent/nutrient constituent)
                   value (or (get-constituent-energy constituent) 0)]
               [{:text (get-nutrient-link db locale nutrient)}
                {:text (get-source food id)
                 :class "mvt-source"}
                {:text (with-source-popover food nutrient
                         (food/get-nutrient-quantity food id))
                 :class "mvt-amount"
                 :data-justify "end"
                 :data-nowrap ""
                 :data-numeric ""}
                {:text (if (= id "Vann")
                         "-"
                         (str (if (zero? total)
                                0
                                (int (* 100 (/ value total)))) "%"))
                 :class "mvt-amount"
                 :data-justify "end"
                 :data-nowrap ""
                 :data-numeric ""}]))}))

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
     :detail [:div (get-kj food)
              (when-let [formatted-kcal (get-kcal food)]
                [:small {:style {:display "block"
                                 :font-size "var(--mtds-body-md-font-size)"
                                 :font-weight "normal"}} formatted-kcal])]
     :href "#naringsinnhold"}]
   (for [[id anchor] [["Fett" "fett"] ["Protein" "energi"] ["Karbo" "karbohydrater"]]]
     (let [constituent (->> (:food/constituents food)
                            (filter (comp #{id} :nutrient/id :constituent/nutrient))
                            first)]
       {:title [:i18n ::highlight-title (:nutrient/name (:constituent/nutrient constituent))]
        :detail [:span (food/wrap-in-portion-span
                        (or (some-> constituent
                                    :measurement/quantity
                                    b/num)
                            0)
                        {:decimals (-> constituent :constituent/nutrient :nutrient/decimal-precision)})
                 (some->> constituent :measurement/quantity b/symbol (str " "))]
        :href (str "#" anchor)}))))

(defn pct [n]
  (str (int (* 100 n)) " %"))

(defn get-recommended-daily-allowance [recommendations measurement]
  (when-let [q (:measurement/quantity measurement)]
    (let [nutrient-id (->> (:constituent/nutrient measurement)
                           :nutrient/id)
          recommendation (recommendations nutrient-id)]
      (when-not (#{"NaCl" "Na"} nutrient-id)
        (when-let [v (or (:rda.recommendation/max-amount recommendation)
                         (:rda.recommendation/min-amount recommendation)
                         (:rda.recommendation/average-amount recommendation))]
          ;; Min/max/average energy percent not yet supported

          [:span.mvt-rda
           {:data-nutrient-id nutrient-id}
           (pct (b// q v))])))))

(defn prepare-nutrient-tables [db
                               {:keys [locale show-header-sum?] :as opt}
                               {:keys [food recommendations nutrients group]}]
  (->> (concat
        [{:headers (->> [{:text (get-nutrient-link db locale group)}
                         {:text [:i18n ::source]
                          :class "mvt-source"
                          :style {:width "var(--mtds-30)"}}
                         {:text (if show-header-sum?
                                  (food/get-nutrient-quantity food (:nutrient/id group))
                                  [:i18n ::amount])
                          :data-numeric ""
                          :data-justify "end"
                          :class "mvt-amount"
                          :style {:width "var(--mtds-30)"}}
                         (when recommendations
                           {:text [:button {:data-popover "inline" :data-tooltip [:i18n ::rda-explanation]}
                                   [:i18n ::rda-pct]]
                            :style {:width "var(--mtds-30)"}})]
                        (remove nil?))
          :rows (for [nutrient nutrients]
                  (->> [{:text (get-nutrient-link db locale nutrient)}
                        {:text (get-source food (:nutrient/id nutrient))
                         :class "mvt-source"}
                        {:text (with-source-popover food nutrient
                                 (food/get-nutrient-quantity food (:nutrient/id nutrient)))
                         :class "mvt-amount"
                         :data-justify "end"
                         :data-nowrap ""
                         :data-numeric ""}
                        (when recommendations
                          {:text (->> (food/get-nutrient-measurement food (:nutrient/id nutrient))
                                      (get-recommended-daily-allowance recommendations))
                           :data-justify "end"
                           :data-nowrap ""
                           :data-numeric ""})]
                       (remove nil?)))}]
        (mapcat
         #(when-let [nutrients (food/get-nutrients food (:nutrient/id %))]
            (prepare-nutrient-tables
             db
             opt
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
                 {:text (with-source-popover food nutrient
                          (food/get-nutrient-quantity food (:nutrient/id nutrient)))
                  :class "mvt-amount"}
                 (when recommendations
                   {:text (->> (food/get-nutrient-measurement food (:nutrient/id nutrient))
                               (get-recommended-daily-allowance recommendations))
                    :data-justify "end"
                    :data-nowrap ""
                    :data-numeric ""})]
                (remove nil?))]
          (let [level (inc level)]
            (->> (:nutrient/id nutrient)
                 (food/get-nutrients food)
                 (mapcat #(get-nutrient-rows food % recommendations db locale level)))))))

(defn prepare-nested-nutrient-table [db locale {:keys [food nutrients group recommendations]}]
  {:headers (->> [{:text (get-nutrient-link db locale group)}
                  {:text [:i18n ::source]
                   :class "mvt-source"
                   :style {:width "var(--mtds-30)"}}
                  {:text [:i18n ::amount]
                   :class "mvt-amount"
                   :style {:width "var(--mtds-30)"}}
                  (when recommendations
                    {:text [:button {:data-popover "inline" :data-tooltip [:i18n ::rda-explanation]}
                            [:i18n ::rda-pct]]
                     :style {:width "var(--mtds-30)"}})]
                 (remove nil?))
   :rows (mapcat #(get-nutrient-rows food % recommendations db locale) nutrients)})

(defn render-table [{:keys [headers rows classes] :as attrs}]
  [:table (merge {:class (mtds/classes :table classes)
                  :data-fixed ""
                  :data-border ""
                  :data-size "sm"
                  :data-align "center"}
                 (dissoc attrs :classes :headers :rows))
   (when headers
     [:thead
      (let [row (if (map? headers) headers {:cols headers})]
        [:tr (dissoc row :cols)
         (for [header (:cols row)]
           [:th (dissoc header :text) (:text header)])])])
   [:tbody
    (for [row rows]
      (let [row (if (map? row) row {:cols row})]
        [:tr (dissoc row :cols)
         (for [cell (:cols row)]
           [(or (:tag cell) :td) (dissoc cell :text :level :tag)
            (cond->> (:text cell)
              (< 0 (or (:level cell) 0))
              (conj [:span {:style {:margin-left (str "var(--mtds-" (* 4 (:level cell)) ")")}}]))])]))]])

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
  {:headers [{:text [:i18n ::langual-code-label]
              :style {:width "12rem"}}
             {:text [:i18n ::langual-description-label]}]
   :rows (for [{:langual-code/keys [id description]} codes]
           [{:text id} {:text (food/humanize-langual-classification description)}])})

(defn ^{:indent 1} render-popover [id & content]
  [:div {:class (mtds/classes :card :popover)
         :data-size "sm"
         :popover "auto"
         :id id
         :style {:max-width "30rem"}}
   [:div {:class (mtds/classes :prose)}
    content]
   [:button {:class (mtds/classes :button)
             :popovertargetaction "hide"
             :data-size "sm"
             :style {:position "absolute"
                     :top "0.5rem"
                     :right "0.5rem"}}
    (icons/render :phosphor.regular/x {:style {:width "1rem" :height "1rem"}})]])

(defn render-sources [page sources]
  (mapv
   (fn [{:source/keys [id description] :as source}]
     (try
       (render-popover (str "source-" id)
         [:h3 {:data-size "sm"} id]
         [:p {:data-size "sm"}
          (-> (get description (:page/locale page))
              food/hyperlink-string)])
       (catch Exception _e
         (throw (ex-info "Failed to render source"
                         {:source (into {} source)
                          :uri (:page/uri page)})))))
   sources))

(def slice-legend
  [{:nutrient-id "Fett"    :color "var(--mtds-color-charts-chart-a)"}
   {:nutrient-id "Karbo"   :color "var(--mtds-color-charts-chart-b)"}
   {:nutrient-id "Protein" :color "var(--mtds-color-charts-chart-c)"}
   {:nutrient-id "Fiber"   :color "var(--mtds-color-charts-chart-d)"}
   {:nutrient-id "Alko"    :color "var(--mtds-color-charts-chart-e)"}
   {:nutrient-id "Vann"    :color "var(--mtds-color-charts-chart-f)"}])

(def nutrient-id->color
  (into {} (map (juxt :nutrient-id :color) slice-legend)))

(defn render-composition-chart [food ids]
  [:mtds-chart {:data-variant "pie" :data-legend "false" :data-aspect "4/3"}
   [:table
    [:thead
     [:tr
      [:th [:i18n ::composition]]
      [:th "gram"]]]
    [:tbody
     (for [id ids]
       (let [constituent (->> (:food/constituents food)
                              (filter (comp #{id} :nutrient/id :constituent/nutrient))
                              first)
             value (or (some-> constituent :measurement/quantity b/num) 0)]
         [:tr
          [:td [:i18n :i18n/lookup (:nutrient/name (:constituent/nutrient constituent))]]
          [:td value]]))]]])

(defn render-energy-chart [food ids]
  [:mtds-chart {:data-variant "pie" :data-legend "false" :data-aspect "4/3"}
   [:table
    [:thead
     [:tr
      [:th [:i18n ::energy-content]]
      [:th "energiprosent"]]]
    [:tbody
     (let [constituents (->> (:food/constituents food)
                             (filter (comp (set ids) :nutrient/id :constituent/nutrient))
                             (map (juxt (comp :nutrient/id :constituent/nutrient) identity))
                             (into {}))
           total (apply + (keep get-constituent-energy (vals constituents)))]
       (when (< 0 total)
         (for [id ids]
           (let [constituent (constituents id)
                 value (or (get-constituent-energy constituent) 0)]
             [:tr
              [:th [:i18n :i18n/lookup (:nutrient/name (:constituent/nutrient constituent))]]
              [:td (Math/round (* 100 (/ value total)))]]))))]]])

(defn passepartout [& body]
  [:div {:class (mtds/classes :grid :card) :data-center "xl" :data-pad "10"}
   [:div {:class (mtds/classes :grid) :data-gap "8" :data-center "md"}
    body]])

(defn passepartout-title [id title & rest]
  [:div {:class (mtds/classes :flex) :data-align "end" :data-justify "space-between"}
   [:h3 {:class (mtds/classes :heading) :data-size "xs" :id id} title]
   rest])

(defn render-rda-select [db selected]
  (let [profiles (rda/get-profiles-per-demographic db)]
    [:div {:class (mtds/classes :field) :data-size "md"}
     [:label [:i18n ::rda-select-label]]
     [:select {:class (mtds/classes :input :mvt-rda-selector)}
      (for [profile profiles]
        [:option (cond-> {:value (:rda/id profile)}
                   (= (:rda/id selected) (:rda/id profile))
                   (assoc :selected "true"))
         [:i18n :i18n/lookup (:rda/demographic profile)]])]]))

(defn render-portion-select [locale portions]
  [:div {:class (mtds/classes :field) :data-size "md"}
   [:label [:i18n ::portion-size]]
   [:select {:class (mtds/classes :input) :id "portion-selector"}
    (into [[:option {:value "100"} [:i18n ::select-grams {:value 100}]]]
          (for [portion portions]
            (let [grams (b/num (:portion/quantity portion))]
              [:option {:value grams}
               [:i18n ::select-portion-with-grams
                {:portion (str "1 " (str/lower-case
                                     (get-in portion [:portion/kind :portion-kind/name locale])))
                 :grams [:i18n :i18n/number {:n grams}]}]])))]])

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
    :href "#klassifisering"}])

(defn render-toc [{:keys [contents class]}]
  [:aside {:data-size "md"}
   (Toc
    {:title [:i18n ::toc-title]
     :contents contents
     :class class})])

(defn render-compare-button [food]
  [:button {:class (mtds/classes :button :mvt-compare-food)
            :type "button"
            :hidden "true"
            :data-size "md"
            :data-variant "secondary"
            :data-food-id (:food/id food)
            :data-food-name [:i18n :i18n/lookup (:food/name food)]}
   (icons/render :phosphor.regular/git-diff)
   [:i18n ::compare-food]])

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

(defn render-foodex2-term [term]
  (str (:foodex2.term/name term) " (" (:foodex2.term/code term) ")"))

(defn render-foodex2-aspect [locale aspect]
  [:a {:href (urls/get-foodex-term-url locale (:foodex2/term aspect))}
   (render-foodex2-term (:foodex2/term aspect))])

(defn render-foodex2-facets [locale food]
  (when-let [facets (->> food :foodex2/classification :foodex2/aspects seq)]
    (render-table
     {:headers [{:text [:i18n ::foodex2-facets-th]
                 :style {:width "12rem"}}
                {:text ""}]
      :rows
      (->> facets
           (group-by (comp (juxt :foodex2.facet/id :foodex2.facet/name) :foodex2/facet))
           (sort-by first)
           (map (fn [[facet-info aspects]]
                  [{:style {:width "12rem"}
                    :text
                    (->> facet-info
                         (remove nil?)
                         (interpose " "))}
                   {:text (interpose ", " (map (partial render-foodex2-aspect locale) aspects))}])))})))

(defn render-foodex2-classification [locale food]
  [:div {:class (mtds/classes :grid)
         :data-gap "6"}
   [:div {:class (mtds/classes :grid)
          :data-gap "2"}
    [:h4#foodex2 "FoodEx2: "
     [:a {:href (urls/get-foodex-term-url locale (-> food :foodex2/classification :foodex2/term))}
      (render-foodex2-term (-> food :foodex2/classification :foodex2/term))]]]
   (render-foodex2-facets locale food)])

(comment
  (do
    (def banankake (d/entity matvaretabellen.dev/foods-db [:food/id "05.448"]))
    (def julekake (d/entity matvaretabellen.dev/foods-db [:food/id "05.097"]))
    (def food banankake))

  )

(defn render [context db page]
  (let [food (d/entity (:foods/db context) [:food/id (:page/food-id page)])
        locale (:page/locale page)
        food-name (get-in food [:food/name locale])
        rda-profile (d/entity (:app/db context) [:rda/id "rda-1179869501"])
        recommendations (->> rda-profile
                             :rda/recommendations
                             (map (juxt :rda.recommendation/nutrient-id identity))
                             (into {}))]
    (layout/layout
     context
     page
     [:head
      [:title food-name]
      (get-open-graph-description food locale)]
     [:body {:data-size "lg"}
      (layout/render-header
       {:locale locale
        :app/config (:app/config context)}
       #(urls/get-food-url % food))
      [:div {:class (mtds/classes :grid) :data-gap "12"}
       [:div {:class (mtds/classes :grid :banner) :data-gap "8" :role "banner"}
        (layout/render-toolbar
         {:locale locale
          :crumbs [(:food/food-group food)
                   {:text (->> (get-in food [:food/name locale])
                               food-name/shorten-name)}]})
        [:div {:class (mtds/classes :flex) :data-center "xl" :data-items "350" :data-gap "12"}
         [:div {:class (mtds/classes :grid) :data-align "end"}
          [:h1 {:class (mtds/classes :heading) :data-size "xl" :style {:align-self "start"}} food-name]
          [:h2 {:class (mtds/classes :heading) :data-size "2xs"} energy-label-mobile]
          [:div {:class (mtds/classes :grid) :data-items "150"}
           (for [{:keys [title detail] :as attr} (prepare-macro-highlights food)]
             [:a
              (-> (dissoc attr :title :detail)
                  (assoc :class (mtds/classes :card :grid)
                         :data-gap "2"))
              [:span {:data-size "md"} title]
              [:div {:class (mtds/classes :heading) :data-size "md"} detail]])]
          [:div.mobile {:class (mtds/classes :grid)} (render-compare-button food)]]
         [:div {:data-fixed ""}
          (render-toc {:contents (get-toc-items)})]]
        (when-let [related (seq (food/find-related-foods food locale))]
          (let [categoryish (food/infer-food-kind food locale)]
            [:div {:class (mtds/classes :flex) :data-center "xl" :data-size "sm"}
             [:strong [:i18n ::more {:categoryish (str/lower-case categoryish)}] " "]
             [:ul {:class (mtds/classes :flex)}
              (for [food related]
                [:li
                 [:a {:href (urls/get-food-url locale food)
                      :data-comparison-suggestion-id (:food/id food)
                      :data-comparison-suggestion-name (get-in food [:food/name locale])}
                  (food/get-variant-name food locale categoryish)]])]]))]

       [:div {:class (mtds/classes :flex) :data-justify "space-between" :data-center "xl" :data-align "end"}
        [:h2#naringsinnhold {:class (mtds/classes :heading) :data-size "md"} [:i18n ::nutrition-title]]
        [:div {:class (mtds/classes :flex) :data-align "end"}
         [:div.desktop (render-compare-button food)]
         (render-portion-select locale (:food/portions food))]]

       [:div {:class (mtds/classes :grid :card) :data-center "xl" :data-pad "10"}
        [:div {:class (mtds/classes :grid) :data-center "md" :data-gap "6"}
         [:h3#energi {:class (mtds/classes :heading) :data-size "xs"} [:i18n ::nutrition-heading]]
         [:div {:class (mtds/classes :flex) :data-items "200" :data-align "center" :data-gap "8"}
          [:div {:class (mtds/classes :flex) :data-justify "center" :data-items "400"}
           [:small {:data-self "auto" :data-fixed ""} [:i18n ::composition]]
           (render-composition-chart food ["Fett" "Karbo" "Protein" "Fiber" "Alko" "Vann"])]
          [:div {:class (mtds/classes :flex) :data-justify "center" :data-items "400"}
           [:small {:data-self "auto" :data-fixed ""} [:i18n ::energy-content]]
           (render-energy-chart food ["Fett" "Karbo" "Protein" "Fiber" "Alko"])]
          [:ul.chart-legend {:class (mtds/classes :grid) :data-gap "1" :data-size "md" :data-self "100"}
           (for [entry slice-legend]
             [:li {:style {:--color (:color entry)}}
              [:i18n :i18n/lookup (:nutrient/name (d/entity db [:nutrient/id (:nutrient-id entry)]))]])]]

         [:div {:class (mtds/classes :flex) :data-justify "space-between" :data-align "end" :data-size "md"}
          [:ul {:class (mtds/classes :grid) :data-gap "0"}
           [:li energy-label ": " (energy food)]
           [:li [:i18n ::edible-part
                 {:pct (-> food :food/edible-part :measurement/percent)}]]]]
         (render-table (prepare-nutrition-table (:app/db context) locale food))]]

       (passepartout
        (passepartout-title "karbohydrater" [:i18n ::carbohydrates-title])
        (->> (food/get-nutrient-group food "Karbo")
             (prepare-nutrient-tables (:app/db context) {:locale locale
                                                         :show-header-sum? true})
             (map render-table)))

       (passepartout
        (passepartout-title "fett" [:i18n ::fat-title])
        (->> (food/get-nutrient-group food "Fett")
             (prepare-nutrient-tables (:app/db context) {:locale locale
                                                         :show-header-sum? true})
             (map render-table)))

       (passepartout
        (->> (render-rda-select (:app/db context) rda-profile)
             (passepartout-title "vitaminer" [:i18n ::vitamins-title]))
        (->> (assoc (food/get-nutrient-group food "FatSolubleVitamins")
                    :recommendations recommendations)
             (prepare-nested-nutrient-table (:app/db context) locale)
             render-table)
        (->> (assoc (food/get-flattened-nutrient-group food "WaterSolubleVitamins")
                    :recommendations recommendations)
             (prepare-nutrient-tables (:app/db context) {:locale locale})
             (map render-table)))

       (passepartout
        (->> (render-rda-select (:app/db context) rda-profile)
             (passepartout-title "mineraler-sporstoffer" [:i18n ::minerals-trace-elements-title]))
        (->> (assoc (food/get-flattened-nutrient-group food "Minerals")
                    :recommendations recommendations)
             (prepare-nutrient-tables (:app/db context) {:locale locale})
             (map #(render-table (assoc % :id "mineraler"))))
        (->> (assoc (food/get-flattened-nutrient-group food "TraceElements")
                    :recommendations recommendations)
             (prepare-nutrient-tables (:app/db context) {:locale locale})
             (map #(render-table (assoc % :id "sporstoffer")))))

       [:div {:class (mtds/classes :grid)
              :data-gap "6"
              :data-center "xl"}
        [:div {:class (mtds/classes :grid)
               :data-gap "0"}
         [:h3#klassifisering {:class (mtds/classes :heading)
                              :data-size "lg"}
          [:i18n ::classification-title]]
         [:p [:i18n ::food-id {:id (:food/id food)}]]
         (when-let [latin-name (not-empty (:food/latin-name food))]
           [:p [:i18n ::scientific-name {:name latin-name}]])]
        (render-foodex2-classification locale food)
        (when-let [langual-codes (seq (food/get-langual-codes food))]
          [:div {:class (mtds/classes :grid)
                 :data-gap "2"}
           [:h4 "LanguaL"]
           [:p [:i18n ::classification-intro
                {:langual-url "https://www.langual.org/"}]]
           (->> langual-codes
                prepare-langual-table
                render-table)])]

       [:div
        (->> (food/get-sources food)
             (render-sources page))]

       (for [{:constituent/keys [nutrient] :as constituent} (:food/constituents food)]
         (when (has-popover? constituent)
           (render-popover (:nutrient/id nutrient)
             [:h3 {:data-size "sm"}
              [:i18n ::nutrient-source-popover-title
               {:nutrient [:i18n :i18n/lookup (:nutrient/name nutrient)]
                :food [:i18n :i18n/lookup (:food/name food)]}]]
             [:table
              {:class (mtds/classes :table)
               :data-size "sm"}
              [:tbody
               [:tr
                [:td [:strong [:i18n ::value-type]]]
                [:td (when-let [value-type (:measurement/value-type constituent)]
                       [:i18n value-type])]]

               [:tr
                [:td [:strong [:i18n ::acquisition-type]]]
                [:td (when-let [acquisition-type (:measurement/acquisition-type constituent)]
                       [:i18n acquisition-type])]]

               [:tr
                [:td [:strong [:i18n ::method-type]]]
                [:td (when-let [method-type (:measurement/method-type constituent)]
                       [:i18n method-type])]]

               [:tr
                [:td [:strong [:i18n ::method-indicator]]]
                [:td (when-let [source (:measurement/method-indicator constituent)]
                       [:i18n :i18n/lookup (:source/description source)])]]]])))

       [:script#food-data
        {:type "text/plain"
         :data-food-id (:food/id food)
         :data-food-name [:i18n :i18n/lookup (:food/name food)]}]

       (comparison/render-comparison-drawer locale)]])))
