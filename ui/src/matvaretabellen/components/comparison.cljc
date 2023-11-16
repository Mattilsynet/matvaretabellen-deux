(ns matvaretabellen.components.comparison
  (:require [fontawesome.icons :as icons]
            [mmm.components.button :refer [Button]]
            [matvaretabellen.urls :as urls]))

(defn render-comparison-drawer [locale]
  [:div.mmm-drawer.mmm-drawer-closed.mvtc-drawer
   [:div.mmm-drawer-content.mmm-flex-middle.mmm-relative
    [:div.mmm-vert-layout-m
     [:h2.mmm-h3 [:i18n ::compare-foods]]
     [:ul.mmm-ul.mmm-horizontal-list.mmm-pills
      [:li [:a.mmm-pill.mmm-actionable
            (icons/render :fontawesome.solid/x {:class :mmm-svg})
            [:span.mvtc-food-name]]]]]
    [:div.mmm-mtm
     [:div.mmm-icon-button.mmm-alr.mmm-actionable.mmm-pos-tr
      (icons/render :fontawesome.solid/x {:class "mmm-svg"})]
     (Button {:text [:i18n ::compare-now]
              :href (urls/get-comparison-url locale)
              :icon :fontawesome.solid/arrow-right
              :icon-position :after
              :class :mmm-mtl})]]])

(defn render-toggle-button [food locale]
  [:span.mmm-icon-button.mmm-actionable.mvt-compare-food
   {:data-food-id (:food/id food)
    :data-food-name (get-in food [:food/name locale])
    :title [:i18n ::stage-for-comparison]}
   (icons/render :fontawesome.solid/code-compare {:class :mmm-svg})])

(defn render-toggle-cell [food locale & [class]]
  {:text (render-toggle-button food locale)
   :class (concat [:mmm-tac :mmm-pas] class)})
