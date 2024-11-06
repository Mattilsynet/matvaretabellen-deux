(ns matvaretabellen.ui.client-table
  (:require [broch.core :as b]
            [fontawesome.icons :as icons]
            [matvaretabellen.food :as food]
            [matvaretabellen.food-group :as food-group]
            [matvaretabellen.nutrient :as nutrient]
            [matvaretabellen.pages.food-page :as food-page]
            [mmm.components.button :refer [Button]]
            [mmm.components.checkbox :refer [Checkbox]]
            [mmm.components.icon-button :refer [IconButton]]))

(def default-checked #{"Fett" "Karbo" "Protein" "Fiber"})

(defn prepare-foods-table [nutrients opt]
  (merge
   {:headers (concat [{:class [:mmm-tac :mmm-pas :mmm-hidden]
                       :data-id "download"
                       :text (IconButton
                              {:class [:mvt-add-to-list]
                               :title [:i18n ::stage-for-download]
                               :icon :fontawesome.solid/arrow-down})}
                      {:text (list [:i18n ::food]
                                   [:span.mvt-sort-icon
                                    (icons/render :fontawesome.solid/sort {:class :mmm-svg})])
                       :class [:mmm-nbr :mmm-sticky-hor]
                       :data-id "foodName"}
                      {:text (list [:i18n ::energy]
                                   [:span.mvt-sort-icon
                                    (icons/render :fontawesome.solid/sort {:class :mmm-svg})])
                       :class [:mmm-nbr]
                       :data-id "energy"}]
                     (for [nutrient nutrients]
                       {:text (list [:i18n :i18n/lookup (:nutrient/name nutrient)]
                                    [:span.mvt-sort-icon
                                     (icons/render :fontawesome.solid/sort {:class :mmm-svg})])
                        :data-id (:nutrient/id nutrient)
                        :class (if (not (default-checked (:nutrient/id nutrient)))
                                 [:mmm-nbr :mmm-tar :mmm-hidden]
                                 [:mmm-nbr :mmm-tar])}))
    :id "filtered-giant-table"
    :classes [:mmm-hidden :mmm-elastic-table]
    :data-page-size 250
    :rows [{:cols
            (concat
             [{:text (IconButton
                      {:class [:mvt-add-to-list]
                       :title [:i18n ::stage-for-download]
                       :icon :fontawesome.solid/arrow-down})
               :class [:mmm-tac :mmm-pas]
               :data-id "download"}
              {:text [:a.mmm-link]
               :class [:mmm-sticky-hor :mmm-et-wrap]
               :data-id "foodName"}
              {:text ""
               :class [:mmm-tar :mmm-nbr]
               :data-id "energy"}]
             (mapv (fn [nutrient]
                     (try
                       {:text (food/get-calculable-quantity
                               {:measurement/quantity (b/from-edn [0 (:nutrient/unit nutrient)])}
                               {:decimals (:nutrient/decimal-precision nutrient)})
                        :class (cond-> [:mmm-tar :mmm-nbr :mvt-amount]
                                 (not (default-checked (:nutrient/id nutrient)))
                                 (conj :mmm-hidden))
                        :data-id (:nutrient/id nutrient)}
                       (catch Exception e
                         (throw (ex-info "Failed to render nutrient"
                                         {:nutrient (into {} nutrient)}
                                         e)))))
                   nutrients))}]}
   opt))

(defn render-filter-list [options]
  (when (seq options)
    [:ul.mmm-ul.mmm-unadorned-list
     (for [filter-m options]
       [:li
        (Checkbox filter-m)
        (render-filter-list (:options filter-m))])]))

(defn render-nutrient-filter-column [filters]
  [:div.mmm-col.mmm-vert-layout-m
   (for [filter-m filters]
     (if (:data-filter-id filter-m)
       (render-filter-list [filter-m])
       (->> (list (when (:label filter-m)
                    [:h3 (select-keys filter-m [:class]) (:label filter-m)])
                  (render-filter-list (:options filter-m)))
            (remove nil?))))])

(defn render-column-settings [foods-db]
  [:div.mmm-container
   [:div.mmm-divider.mmm-vert-layout-m.mmm-bottom-divider.mmm-hidden#columns-panel
    [:div.mmm-cols.mmm-twocols
     (->> (nutrient/prepare-filters foods-db {:columns 2})
          (map render-nutrient-filter-column))]]])

(defn render-food-group-settings [context page]
  [:div.mmm-container
   [:div.mmm-divider.mmm-vert-layout-m.mmm-col.mmm-hidden#food-group-panel
    (food-group/render-food-group-filters
     (:app/db context)
     (food-group/get-food-groups (:foods/db context))
     nil
     (:page/locale page))]])

(defn render-table-skeleton [foods-db & [opt]]
  (let [nutrients (->> (nutrient/get-used-nutrients foods-db)
                       nutrient/sort-by-preference)]
    [:div.mmm-sidescroller.mmm-col
     [:div.mmm-hidden
      (icons/render :fontawesome.solid/sort {:class [:mmm-svg :mvt-sort]})
      (icons/render :fontawesome.solid/arrow-up-wide-short {:class [:mmm-svg :mvt-desc]})
      (icons/render :fontawesome.solid/arrow-down-short-wide {:class [:mmm-svg :mvt-asc]})]
     (->> (prepare-foods-table nutrients opt)
          food-page/render-table)
     [:div.mmm-buttons.mmm-mvm
      (Button
       {:text [:i18n ::prev]
        :class [:mvt-prev :mmm-hidden]
        :secondary? true
        :inline? true
        :icon :fontawesome.solid/chevron-left})
      (Button
       {:text [:i18n ::next]
        :class [:mvt-next :mmm-hidden]
        :secondary? true
        :inline? true
        :icon :fontawesome.solid/chevron-right
        :icon-position :after})]]))

(defn render-food-groups-toggle []
  (IconButton
   {:label [:i18n ::food-groups]
    :data-toggle-target "#food-group-panel"
    :icon :fontawesome.solid/gear}))

(defn render-nutrients-toggle []
  (IconButton
   {:label [:i18n ::columns]
    :data-toggle-target "#columns-panel"
    :icon :fontawesome.solid/table}))

(defn render-download-csv-button []
  (Button
   {:text [:i18n ::download]
    :secondary? true
    :inline? true
    :download [:i18n ::file-name]
    :href "#"
    :class [:mmm-button-small :mvt-download :mmm-hidden]
    :icon :fontawesome.solid/arrow-down}))
