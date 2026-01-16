(ns matvaretabellen.layout
  (:require [clojure.java.io :as io]
            [mattilsynet.design :as m]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.ui.breadcrumbs :refer [Breadcrumbs]]
            [matvaretabellen.ui.search-input :refer [SearchInput]]
            [matvaretabellen.urls :as urls]))

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
      :xmlns "http://www.w3.org/2000/svg"
      :style {:color "var(--mtds-color-base-default)"}}
     [:use {:xlink:href (str illustration "#illustration")}]]))

(defn Footer [{:keys [cols]}]
  [:footer.footer.screen-sm-inline-pad {:data-color "inverted"}
   [:div {:class (m/c :grid)
          :data-items "300"
          :data-align "start"
          :data-size "sm"
          :data-center "xl"
          :data-gap "8"}
    [:a {:href "https://www.mattilsynet.no"
         :aria-label "Mattilsynet"
         :class (m/c :logo)}]
    (for [{:keys [title items]} cols]
      [:div {:class (m/c :grid)}
       [:h2 {:class (m/c :heading) :data-size "sm"} title]
       (when (seq items)
         [:ul {:class (m/c :grid)}
          (for [{:keys [url text]} items]
            [:li (if url
                   [:a {:href url} text]
                   text)])])])]])

(defn CompactSiteFooter [{:page/keys [locale]}]
  (Footer
   {:cols [{:title [:i18n ::shortcuts-title]
            :items [{:url (if (= :en locale)
                            "/en/api/"
                            "/api/")
                     :text [:i18n ::api-text]}
                    {:text [:i18n ::about-us]
                     :url [:i18n ::about-url]}
                    {:url "https://www.mattilsynet.no/om-mattilsynet/personvernerklaering"
                     :text [:i18n ::privacy-and-cookies]}
                    {:url "https://www.mattilsynet.no/"
                     :text "mattilsynet.no"}]}
           {:title [:i18n ::about-mattilsynet]
            :items [{:url "mailto:matvaretabellen@mattilsynet.no"
                     :text "matvaretabellen@mattilsynet.no"}]}]}))

(defn layout [context page head body]
  [:html {:data-color-scheme "auto"}
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
     (CompactSiteFooter page)))])

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

(defn SiteHeader [{:keys [home-url extra-link extra-links]}]
  [:header.header
   [:div {:class (m/c :flex)
          :data-center "xl"
          :data-justify "space-between"
          :data-align "center"}
    [:a {:class (m/c :logo)
         :href home-url}
     "Matvaretabellen"]
    (when extra-link
      [:a {:href (:url extra-link)}
       (:text extra-link)])
    (when extra-links
      [:menu {:class (m/c :flex)}
       (for [{:keys [url text class]} extra-links]
         [:li {:class class}
          (if url
            [:a {:href url
                 :class (m/c :button)
                 :data-variant "tertiary"} text]
            [:span {:class (m/c :button)
                    :data-variant "tertiary"
                    :aria-current "page"} text])])])]])

(defn render-header [{:keys [locale app/config]} get-current-url]
  [:div#header
   (SiteHeader
    {:home-url (let [url (urls/get-base-url locale)]
                 (when-not (= url (get-current-url locale))
                   url))
     :extra-links (prepare-header-links locale get-current-url)
     :theme (:app/theme config)})])

(defn render-toolbar [{:keys [locale crumbs]}]
  [:div {:class (m/c :flex) :data-justify "space-between" :data-center "xl" :data-gap "6" :data-items "350"}
   (Breadcrumbs
    {:links (apply crumbs/crumble locale crumbs)})
   [:form
    {:action (urls/get-search-url locale)
     :method :get
     :data-fixed ""}
    [:div
     (SearchInput
      {:button {:text [:i18n :i18n/search-button]}
       :class :mvt-autocomplete
       :input {:name "q"
               :data-suggestions "8"
               :placeholder [:i18n :i18n/search-label]}
       :autocomplete-id "foods-results"
       :default-value [:i18n :i18n/search-default-value]
       :size :small})]]])
