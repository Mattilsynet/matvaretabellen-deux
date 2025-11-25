(ns matvaretabellen.components.comparison
  (:require [matvaretabellen.urls :as urls]
            [mmm.components.button :refer [Button]]
            [mmm.components.icon-button :refer [IconButton]]
            [phosphor.icons :as icons]))

(defn render-comparison-drawer [locale]
  [:div.mmm-drawer.mmm-drawer-closed.mvtc-drawer
   [:div.mmm-drawer-content.mmm-relative.mmm-vert-layout-m
    [:h2.mmm-h3 [:i18n ::compare-foods]]
    [:div.mmm-icon-button.mmm-alr.mmm-actionable.mmm-pos-tr
     (icons/render :phosphor.regular/x {:class "mmm-svg"})]
    [:div.mmm-flex-desktop.mmm-flex-middle.mmm-mobile-vert-layout-m
     [:ul.mmm-ul.mmm-horizontal-list.mmm-pills
      [:li [:a.mmm-pill.mmm-actionable
            (icons/render :phosphor.regular/x {:class :mmm-svg})
            [:span.mvtc-food-name]]]]
     [:div
      (Button {:text [:i18n ::compare-now]
               :href (urls/get-comparison-url locale)
               :icon :phosphor.regular/arrow-right
               :icon-position :after})]]]
   [:div.mmm-footnote.mmm-small.mvtc-suggestions.mmm-inline
    [:strong [:i18n ::suggestions]]
    [:ul.mmm-ul.mmm-horizontal-list.mmm-hl-divider
     [:li.mmm-subtle [:a.mmm-link]]]]])

(defn render-toggle-button [food locale]
  (IconButton
   {:class [:mvt-compare-food]
    :data-food-id (:food/id food)
    :data-food-name (get-in food [:food/name locale])
    :title [:i18n ::stage-for-comparison]
    :icon :phosphor.regular/git-diff}))

(defn render-toggle-cell [food locale & [class]]
  {:text (render-toggle-button food locale)
   :class (concat [:mmm-tac :mmm-pas] class)})
