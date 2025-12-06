(ns matvaretabellen.ui.comparison
  (:require [mattilsynet.design :as mtds]
            [matvaretabellen.urls :as urls]
            [phosphor.icons :as icons]))

(defn render-comparison-drawer [locale]
  [:dialog.mvtc-drawer.drawer {:class (mtds/classes :dialog) :data-size "md" :data-modal "false"}
   [:div {:class (mtds/classes :grid) :data-gap "2"}
    [:div {:class (mtds/classes :flex) :data-justify "space-between" :data-align "center"}
     [:h2 {:class (mtds/classes :heading) :data-size "xs"} [:i18n ::compare-foods]]
     [:button.mvtc-drawer-close {:class (mtds/classes :button) :type "button"}
      (icons/render :phosphor.regular/x)]]
    [:div {:class (mtds/classes :flex) :data-justify "space-between" :data-align "center"}
     [:ul.mvtc-drawer-foods {:class (mtds/classes :flex)}
      [:li [:button {:class (mtds/classes :chip :mvtc-food-name) :data-removable "" :type "button"}]]]
     [:a {:class (mtds/classes :button :mvtc-drawer-compare)
          :data-variant "primary"
          :href (urls/get-comparison-url locale)}
      [:i18n ::compare-now]
      (icons/render :phosphor.regular/arrow-right)]]
    [:div.mvtc-suggestions {:class (mtds/classes :grid) :data-size "sm" :data-gap "1"}
     [:strong [:i18n ::suggestions]]
     [:ul {:class (mtds/classes :flex)}
      [:li [:a]]]]]])

(defn render-toggle-button [food locale]
  [:button {:class (mtds/classes :button :mvt-compare-food)
            :type "button"
            :aria-label [:i18n ::stage-for-comparison]
            :data-size "sm"
            :data-food-id (:food/id food)
            :data-food-name (get-in food [:food/name locale])}
   (icons/render :phosphor.regular/git-diff)])

(defn render-toggle-cell [food locale & [class]]
  {:text (render-toggle-button food locale)
   :class class
   :data-justify "center"})
