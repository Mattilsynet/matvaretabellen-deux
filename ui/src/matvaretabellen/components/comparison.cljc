(ns matvaretabellen.components.comparison
  (:require [matvaretabellen.urls :as urls]
            [phosphor.icons :as icons]
            [mattilsynet.design :as mtds]))

(defn render-comparison-drawer [locale]
  [:div.mmm-drawer.mmm-drawer-closed.mvtc-drawer {:data-size "md"}
   [:div {:class (mtds/classes :grid)}
    [:div {:class (mtds/classes :flex) :data-justify "space-between" :data-align "center"}
     [:h2 {:class (mtds/classes :heading) :data-size "xs"} [:i18n ::compare-foods]]
     [:button.mmm-icon-button {:class (mtds/classes :button) :type "button"} ;; TODO EIRIK: mmm-class needed in JS
      (icons/render :phosphor.regular/x)]]
    [:div {:class (mtds/classes :flex) :data-justify "space-between" :data-align "end"}
     [:ul.mmm-pills {:class (mtds/classes :flex)} ;; TODO EIRIK: mmm-class needed in JS
      [:li [:a.mmm-pill.mmm-actionable {:class (mtds/classes :chip) :data-removable ""}
            [:span.mvtc-food-name]]]]
     [:a {:class (mtds/classes :button :mmm-button) ;; TODO EIRIK: mmm-class needed in JS
          :data-variant "primary"
          :href (urls/get-comparison-url locale)}
      [:i18n ::compare-now]
      (icons/render :phosphor.regular/arrow-right)]]]
   [:div.mvtc-suggestions {:class (mtds/classes :grid) :data-size "sm"}
    [:strong [:i18n ::suggestions]]
    [:ul {:class (mtds/classes :flex)}
     [:li [:a]]]]])

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
