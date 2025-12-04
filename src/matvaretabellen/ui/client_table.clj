(ns matvaretabellen.ui.client-table
  (:require [broch.core :as b]
            [mattilsynet.design :as mtds]
            [matvaretabellen.food :as food]
            [matvaretabellen.food-group :as food-group]
            [matvaretabellen.nutrient :as nutrient]
            [matvaretabellen.pages.food-page :as food-page]
            [phosphor.icons :as icons]))

(def default-checked #{"Fett" "Karbo" "Protein" "Fiber"})

(defn prepare-foods-table [nutrients opt]
  (merge
   {:headers (concat [{:hidden "true"
                       :data-id "download"
                       :style {:width "var(--mtds-15)"}
                       :text [:button {:class (mtds/classes :button :mvt-add-to-list)
                                       :type "button"
                                       :aria-label [:i18n ::stage-for-download]
                                       :data-size "sm"}
                              (icons/render :phosphor.regular/arrow-down)]}
                      {:text [:button {:type "button"}
                              [:i18n ::food]]
                       :aria-sort "none"
                       :data-nowrap ""
                       :data-id "foodName"}
                      {:text [:button {:type "button"}
                              [:i18n ::energy]]
                       :aria-sort "none"
                       :data-nowrap ""
                       :data-id "energy"}]
                     (for [nutrient nutrients]
                       (cond-> {:text [:button {:type "button"}
                                       [:i18n :i18n/lookup (:nutrient/name nutrient)]]
                                :data-id (:nutrient/id nutrient)
                                :data-nowrap ""
                                :aria-sort "none"}
                         (not (default-checked (:nutrient/id nutrient)))
                         (assoc :hidden "true"))))
    :id "filtered-giant-table"
    :hidden "true"
    :data-page-size 250
    :rows [{:cols
            (concat
             [{:text [:button {:class (mtds/classes :button :mvt-add-to-list)
                               :type "button"
                               :aria-label [:i18n ::stage-for-download]
                               :data-size "sm"}
                      (icons/render :phosphor.regular/arrow-down)]
               :data-id "download"}
              {:text [:a]
               :data-id "foodName"}
              {:text ""
               :data-id "energy"
               :data-justify "end"
               :data-numeric ""}]
             (mapv (fn [nutrient]
                     (try
                       (cond-> {:text (food/get-calculable-quantity
                                       {:measurement/quantity (b/from-edn [0 (:nutrient/unit nutrient)])}
                                       {:decimals (:nutrient/decimal-precision nutrient)})
                                :class [:mvt-amount]
                                :data-justify "end"
                                :data-nowrap ""
                                :data-numeric ""
                                :data-id (:nutrient/id nutrient)}
                         (not (default-checked (:nutrient/id nutrient)))
                         (assoc :hidden "true"))
                       (catch Exception e
                         (throw (ex-info "Failed to render nutrient"
                                         {:nutrient (into {} nutrient)}
                                         e)))))
                   nutrients))}]}
   opt))

(defn render-filter-list [options]
  (when (seq options)
    [:ul {:class (mtds/classes :grid) :data-gap "1"}
     (for [filter-m options]
       [:li
        [:div {:class (mtds/classes :grid) :data-gap "1"}
         [:div {:class (mtds/classes :field)}
          [:input {:class (mtds/classes :input) :type "checkbox" :checked (when (:checked? filter-m) "true")}]
          [:label (dissoc filter-m :checked? :label)
           (:label filter-m)]]
         [:div {:style {:margin-left "var(--mtds-8)"}}
          (render-filter-list (:options filter-m))]]])]))

(defn render-nutrient-filter-column [filters]
  [:div {:class (mtds/classes :grid)}
   (for [filter-m filters]
     (if (:data-filter-id filter-m)
       (render-filter-list [filter-m])
       (->> (list (when (:label filter-m)
                    [:h3 (select-keys filter-m [:class]) (:label filter-m)])
                  (render-filter-list (:options filter-m)))
            (remove nil?))))])

(defn render-column-settings [foods-db]
  [:div#columns-panel {:class (mtds/classes :grid :group)
                       :data-items "350"
                       :data-align "start"
                       :data-size "md"
                       :hidden "true"}
   (->> (nutrient/prepare-filters foods-db {:columns 2})
        (map render-nutrient-filter-column))])

(defn render-food-group-settings [context page]
  [:div#food-group-panel {:class (mtds/classes :grid :group)
                          :data-items "350"
                          :data-align "start"
                          :data-size "md"
                          :hidden "true"}
   (food-group/render-food-group-filters
    (:app/db context)
    (food-group/get-food-groups (:foods/db context))
    nil
    (:page/locale page))])

(defn render-table-skeleton [foods-db & [opt]]
  (let [nutrients (->> (nutrient/get-used-nutrients foods-db)
                       nutrient/sort-by-preference)]
    [:figure {:class (mtds/classes :grid) :data-gap "8"}
     ;; TODO EIRIK: Remove when fully using aria-sort
     [:div {:hidden "true"}
      (icons/render :phosphor.regular/caret-up-down {:class [:mvt-sort]})
      (icons/render :phosphor.regular/sort-descending {:class [:mvt-desc]})
      (icons/render :phosphor.regular/sort-ascending {:class [:mvt-asc]})]
     (->> (prepare-foods-table nutrients opt)
          food-page/render-table)
     [:div {:class (mtds/classes :flex)}
      [:button {:class (mtds/classes :button :mvt-prev)
                :hidden "true"
                :data-variant "secondary"
                :type "button"}
       (icons/render :phosphor.regular/caret-left)
       [:i18n ::prev]]
      [:button {:class (mtds/classes :button :mvt-next)
                :hidden "true"
                :data-variant "secondary"
                :type "button"}
       [:i18n ::next]
       (icons/render :phosphor.regular/caret-right)]]]))

(defn render-food-groups-toggle []
  [:button {:class (mtds/classes :button :mvt-add-to-list)
            :data-toggle-target "#food-group-panel"
            :type "button"}
   (icons/render :phosphor.regular/gear)
   [:i18n ::food-groups]])

(defn render-nutrients-toggle []
  [:button {:class (mtds/classes :button :mvt-add-to-list)
            :data-toggle-target "#columns-panel"
            :type "button"}
   (icons/render :phosphor.regular/table)
   [:i18n ::columns]])

(defn render-download-csv-button []
  [:a {:class (mtds/classes :button :mvt-download)
       :hidden "true"
       :data-variant "secondary"
       :href "#"
       :download [:i18n ::file-name]}
   (icons/render :phosphor.regular/arrow-down)
   [:span
    [:i18n ::download]]])
