(ns matvaretabellen.layout
  (:require [matvaretabellen.crumbs :as crumbs]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.footer :refer [CompactSiteFooter]]
            [mmm.components.search-input :refer [SearchInput]]))

(defn layout [context head body]
  [:html {:class "mmm"}
   head
   (into
    body
    (list [:div.mmm-container.mmm-section
           (CompactSiteFooter)]
          [:img {:src (str "https://mattilsynet.matomo.cloud/matomo.php?idsite="
                           (:matomo/site-id context) "&rec=1")
                 :style "border:0"
                 :alt ""}]))])

(defn render-toolbar [{:keys [locale crumbs]}]
  [:div.mmm-container.mmm-section.mmm-flex-desktop.mmm-flex-desktop-middle.mmm-mobile-vert-layout-m
   (Breadcrumbs
    {:links (apply crumbs/crumble locale crumbs)})
   [:form.mvt-aside-col
    (SearchInput
     {:button {:text [:i18n :i18n/search-button]}
      :input {:name "foods-search"
              :data-suggestions "8"}
      :autocomplete-id "foods-results"
      :size :small})]])
