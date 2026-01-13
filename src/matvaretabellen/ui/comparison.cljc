(ns matvaretabellen.ui.comparison
  (:require [mattilsynet.design :as m]
            [matvaretabellen.urls :as urls]
            [phosphor.icons :as icons]))

(defn render-comparison-drawer [locale]
  [:dialog.mvtc-drawer.drawer {:class (m/c :dialog) :data-size "md" :data-modal "false"}
   [:div {:class (m/c :grid) :data-gap "2"}
    [:div {:class (m/c :flex) :data-justify "space-between" :data-align "center"}
     [:h2 {:class (m/c :heading) :data-size "xs"} [:i18n ::compare-foods]]
     [:button.mvtc-drawer-close {:class (m/c :button) :type "button"}
      (icons/render :phosphor.regular/x)]]
    [:div {:class (m/c :flex) :data-justify "space-between" :data-align "center"}
     [:ul.mvtc-drawer-foods {:class (m/c :flex)}
      [:li [:button {:class (m/c :chip :mvtc-food-name) :data-removable "" :type "button"}]]]
     [:a {:class (m/c :button :mvtc-drawer-compare)
          :data-variant "primary"
          :href (urls/get-comparison-url locale)}
      [:i18n ::compare-now]
      (icons/render :phosphor.regular/arrow-right)]]
    [:div.mvtc-suggestions {:class (m/c :grid) :data-size "sm" :data-gap "1"}
     [:strong [:i18n ::suggestions]]
     [:ul {:class (m/c :flex)}
      [:li [:a]]]]]])

(defn render-toggle-button [food locale]
  [:button {:class (m/c :button :mvt-compare-food)
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
