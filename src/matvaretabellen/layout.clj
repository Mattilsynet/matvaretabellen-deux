(ns matvaretabellen.layout
  (:require [mattilsynet.design :as mtds])
  (:require [clojure.java.io :as io]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.footer :refer [CompactSiteFooter]]
            [mmm.components.search-input :refer [SearchInput]]
            [mmm.components.site-header :refer [SiteHeader]]
            [phosphor.icons :as icons]
            [mattilsynet.design :as mtds]))

(def report-errors-to-tracer
  "
var tracerStatus = {};
window.onerror = function(message) {
  if (tracerStatus.reported) { return; }
  tracerStatus.reported = true;
  var xhr = new XMLHttpRequest();
  xhr.open('GET', '/tracer/report/?error=' + encodeURIComponent(message), true);
  xhr.send();
};
")

(defn render-illustration [illustration]
  (when-let [view-box (some->> illustration
                               (str "public")
                               io/resource
                               slurp
                               (re-find #"viewBox=\"([^\"]+)\"")
                               last)]
    [:svg.mvt-illustration
     {:viewBox view-box
      :xmlns "http://www.w3.org/2000/svg"}
     [:use {:xlink:href (str illustration "#illustration")}]]))

(defn layout [context _page head body]
  [:html {:class [:mmm (:app/theme (:app/config context))] :data-color-scheme "auto"}
   (into
    head
    (list [:script {:type "text/javascript"} report-errors-to-tracer]
          [:link {:rel "icon" :href "/mtds/favicon.svg" :type "image/svg+xml"}]
          [:link {:rel "icon" :href "/mtds/favicon.ico" :media "(prefers-color-scheme: light)"}]
          [:link {:rel "icon" :href "/mtds/favicon-dark.ico" :media "(prefers-color-scheme: dark)"}]))
   (into
    body
    (list
      [:img {:data-src (str "https://mattilsynet.matomo.cloud/matomo.php?idsite="
                            (:matomo/site-id context)
                            "&rec=1"
                            "&url={url}"
                            "&action_name={title}"
                            "&ua={ua}"
                            "&urlref={referrer}")
             :id "mvt-tracking-pixel"
             :style "border:0"
             :alt ""}]
       (CompactSiteFooter (:app/config context))))])

(defn prepare-header-links [locale get-current-url]
  (let [current-url (get-current-url locale)]
    (for [link [{:text [:i18n ::food-groups]
                 :url (urls/get-food-groups-url locale)
                 :class :desktop}
                {:text [:i18n ::nutrients]
                 :url (urls/get-nutrients-url locale)
                 :class :desktop}
                {:text [:i18n :i18n/other-language]
                 :url (get-current-url ({:en :nb :nb :en} locale))}]]
      (cond-> link
        (= current-url (:url link))
        (dissoc :url)))))

(defn render-header [{:keys [locale app/config]} get-current-url]
  [:div#header
   [:script {:type "text/javascript"}
    "document.body.classList.add(\"mmm-js-enabled\");"]
   (SiteHeader
    {:home-url (let [url (urls/get-base-url locale)]
                 (when-not (= url (get-current-url locale))
                   url))
     :extra-links (prepare-header-links locale get-current-url)
     :theme (:app/theme config)})])

(defn render-toolbar [{:keys [locale crumbs]}]
  [:div {:class (mtds/classes :flex) :data-justify "space-between" :data-center "xl" :data-gap "6" :data-items "350"}
   (Breadcrumbs
    {:links (apply crumbs/crumble locale crumbs)})
   [:form
    {:action (urls/get-search-url locale)
     :method :get
     :data-fixed ""}
    [:div.mmm-js-required
     (SearchInput
      {:button {:text [:i18n :i18n/search-button]}
       :class :mvt-autocomplete
       :input {:name "q"
               :data-suggestions "8"
               :placeholder [:i18n :i18n/search-label]}
       :autocomplete-id "foods-results"
       :size :small})]]])