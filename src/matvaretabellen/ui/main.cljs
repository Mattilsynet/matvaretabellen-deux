(ns ^:figwheel-hooks matvaretabellen.ui.main
  (:require [matvaretabellen.ui.comparison :as comparison]
            [matvaretabellen.ui.dom :as dom]
            [matvaretabellen.ui.filters :as filters]
            [matvaretabellen.ui.hoverable :as hoverable]
            [matvaretabellen.ui.portions :as portions]
            [matvaretabellen.ui.rda :as rda]
            [matvaretabellen.ui.search :as search-ui]
            [matvaretabellen.ui.sidebar :as sidebar]
            [matvaretabellen.ui.sources :as sources]
            [matvaretabellen.ui.table :as table]
            [matvaretabellen.ui.tabs :as tabs]
            [matvaretabellen.ui.toggler :as toggler]
            [matvaretabellen.ui.tracking :as tracking]
            [matvaretabellen.urls :as urls]))

(defn ^:after-load main []
  (search-ui/populate-search-engine js/document.documentElement.lang))

(defn ensure-session-data [k url f & [{:keys [process-raw-data]}]]
  (try
    (if-let [data (dom/get-session-json k)]
      (f data)
      (-> (js/fetch url)
          (.then (fn [res] (.json res)))
          (.then (fn [raw-data]
                   (let [data (cond-> raw-data
                                (ifn? process-raw-data) process-raw-data)]
                     (dom/set-session-json k data)
                     (f data))))))
    (catch :default e
      (js/console.error "Failed to load session data" e))))

(defn map-json-by [k data]
  (->> (js->clj data)
       (map (juxt #(get % k) identity))
       (into {})
       clj->js))

(defn ensure-food-data [k locale f]
  (ensure-session-data
   k (urls/get-compact-foods-json-url locale) f
   {:process-raw-data #(map-json-by "id" %)}))

(defn ensure-rda-data [k locale f]
  (ensure-session-data
   k (urls/get-api-rda-json-url locale) f
   {:process-raw-data #(map-json-by "id" (aget % "profiles"))}))

(defn boot []
  (main)
  (let [locale (keyword js/document.documentElement.lang)
        event-bus (atom nil)]
    (tracking/track-page-view)

    (search-ui/initialize-foods-autocomplete
     (dom/qs ".mvt-autocomplete")
     locale
     (get (dom/get-params) "search"))

    (portions/initialize-portion-selector
     js/document.documentElement.lang
     (js/document.querySelector "#portion-selector")
     (js/document.querySelectorAll "[data-portion]")
     (js/document.querySelectorAll ".js-portion-label")
     event-bus)

    (sources/initialize-source-toggler ".mvt-source-toggler")

    (comparison/initialize-tooling ".mvt-compare-food" ".mvtc-drawer")

    (let [k (str "food-data-" (name locale))]
      (->> (fn [data]
             (when (= "comparison" js/document.body.id)
               (comparison/initialize-page data locale (dom/get-params)))
             (let [mother-of-all-tables (js/document.getElementById "filtered-giant-table")]
               (when mother-of-all-tables
                 (table/init-giant-table
                  data
                  locale
                  {:column-panel (js/document.getElementById "columns-panel")
                   :filter-panel (js/document.getElementById "food-group-panel")
                   :table mother-of-all-tables
                   :download-buttons (dom/qsa ".mvt-download")
                   :clear-download-buttons (dom/qsa ".mvt-clear-downloads")}
                  {:params (dom/get-params)}))))
           (ensure-food-data k locale)))

    (when-let [selects (dom/qsa ".mvt-rda-selector")]
      (ensure-rda-data
       (str "rda-data-" (name locale))
       locale
       #(rda/initialize-rda-selectors (js->clj %) selects event-bus)))

    (sidebar/initialize ".mvt-sidebar-toggle")

    (let [panel (dom/qs ".mvt-food-group-filters")
          table (dom/qs ".mvt-filtered-table")]
      (when (and panel table)
        (filters/initialize-filter panel table))))

  (toggler/init)
  (tabs/init)
  (hoverable/set-up js/document))

(defonce ^:export kicking-out-the-jams (boot))
