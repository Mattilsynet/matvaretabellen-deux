(ns matvaretabellen.layout
  (:require [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.button :refer [Button]]
            [mmm.components.footer :refer [CompactSiteFooter]]
            [mmm.components.icon-button :refer [IconButton]]
            [mmm.components.search-input :refer [SearchInput]]
            [mmm.components.site-header :refer [SiteHeader]]))

(defn layout [context page head body]
  [:html {:class "mmm"}
   head
   (into
    body
    (list [:div.mmm-container.mmm-section
           (CompactSiteFooter)]
          [:img {:src (str "https://mattilsynet.matomo.cloud/matomo.php?idsite="
                           (:matomo/site-id context)
                           "&rec=1"
                           "&action_name=" (:page/uri page))
                 :style "border:0"
                 :alt ""}]))])

(defn prepare-header-links [locale get-current-url]
  (let [current-url (get-current-url locale)]
    (for [link [{:text [:i18n ::food-groups]
                 :url (urls/get-food-groups-url locale)
                 :class :mmm-desktop}
                {:text [:i18n ::nutrients]
                 :url (urls/get-nutrients-url locale)
                 :class :mmm-desktop}
                {:text [:i18n :i18n/other-language]
                 :class :mvt-other-lang
                 :url (get-current-url ({:en :nb :nb :en} locale))}]]
      (cond-> link
        (= current-url (:url link))
        (dissoc :url)))))

(defn render-header [locale get-current-url]
  (SiteHeader
   {:home-url (let [url (urls/get-base-url locale)]
                (when-not (= url (get-current-url locale))
                  url))
    :extra-links (prepare-header-links locale get-current-url)}))

(defn render-toolbar [{:keys [locale crumbs]}]
  [:div.mmm-container.mmm-section.mmm-flex-desktop.mmm-flex-desktop-middle.mmm-mobile-vert-layout-m
   (Breadcrumbs
    {:links (apply crumbs/crumble locale crumbs)})
   [:form.mvt-aside-col.mvt-search-col
    (SearchInput
     {:button {:text [:i18n :i18n/search-button]}
      :input {:name "foods-search"
              :data-suggestions "8"
              :placeholder [:i18n :i18n/search-label]}
      :autocomplete-id "foods-results"
      :size :small})]])

(defn render-sidebar-filter-button [target-id]
  [:p.mmm-p.mmm-mobile
   (Button
    {:text [:i18n ::filter]
     :class [:mvt-sidebar-toggle]
     :data-sidebar-target (str "#" target-id)
     :icon :fontawesome.solid/filter
     :inline? true
     :secondary? true})])

(defn render-sidebar-close-button [target-id]
  (IconButton
   {:title [:i18n ::close-sidebar]
    :class :mvt-sidebar-toggle
    :data-sidebar-target (str "#" target-id)
    :icon :fontawesome.solid/x}))
