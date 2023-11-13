(ns matvaretabellen.components.comparison
  (:require [fontawesome.icons :as icons]
            [mmm.components.button :refer [Button]]
            [matvaretabellen.urls :as urls]))

(defn render-comparison-drawer [locale]
  [:div.mmm-drawer.mmm-drawer-closed.mvtc-drawer
   [:div.mmm-drawer-content.mmm-flex.mmm-flex-middle
    [:div.mmm-vert-layout-m
     [:h2.mmm-h3 [:i18n ::compare-foods]]
     [:ul.mmm-ul.mmm-horizontal-list.mmm-pills
      [:li [:a.mmm-pill.mmm-actionable
            (icons/render :fontawesome.solid/x {:class :mmm-svg})
            [:span.mvtc-food-name]]]]]
    [:div
     (Button {:text [:i18n ::compare-now]
              :href (urls/get-comparison-url locale)
              :icon :fontawesome.solid/arrow-right
              :icon-position :after})]]])
