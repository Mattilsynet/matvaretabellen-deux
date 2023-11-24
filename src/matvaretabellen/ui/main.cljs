(ns ^:figwheel-hooks matvaretabellen.ui.main
  (:require [clojure.string :as str]
            [matvaretabellen.ui.comparison :as comparison]
            [matvaretabellen.ui.dom :as dom]
            [matvaretabellen.ui.filters :as filters]
            [matvaretabellen.ui.hoverable :as hoverable]
            [matvaretabellen.ui.portions :as portions]
            [matvaretabellen.ui.rda :as rda]
            [matvaretabellen.ui.search :as search-ui]
            [matvaretabellen.ui.sidebar :as sidebar]
            [matvaretabellen.ui.sources :as sources]
            [matvaretabellen.ui.table :as table]
            [matvaretabellen.ui.toggler :as toggler]
            [matvaretabellen.urls :as urls]))

(defn ^:after-load main []
  (search-ui/populate-search-engine js/document.documentElement.lang))

(defn get-params []
  (when (seq js/location.search)
    (update-vals (apply hash-map (str/split (subs js/location.search 1) #"[=&]"))
                 #(js/decodeURIComponent %))))

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
    (catch :default _e
      (f nil))))

(defn map-json-by [k data]
  (->> (js->clj data)
       (map (juxt #(get % k) identity))
       (into {})
       clj->js))

(defn ensure-comparison-data [k locale f]
  (ensure-session-data
   k (urls/get-api-foods-json-url locale) f
   {:process-raw-data #(map-json-by "id" %)}))

(defn ensure-rda-data [k locale f]
  (ensure-session-data
   k (urls/get-api-rda-json-url locale) f
   {:process-raw-data #(map-json-by "id" (aget % "profiles"))}))

(defn boot []
  (main)
  (let [locale (keyword js/document.documentElement.lang)
        event-bus (atom nil)]
    (search-ui/initialize-foods-autocomplete
     (js/document.querySelector ".mmm-search-input")
     locale
     (get (get-params) "search"))

    (portions/initialize-portion-selector
     js/document.documentElement.lang
     (js/document.querySelector "#portion-selector")
     (js/document.querySelectorAll "[data-portion]")
     (js/document.querySelectorAll ".js-portion-label")
     event-bus)

    (sources/initialize-source-toggler ".mvt-source-toggler")

    (comparison/initialize-tooling ".mvt-compare-food" ".mvtc-drawer")

    (let [k (str "comparison-data-" (name locale))]
      (->> (fn [data]
             (when (= "comparison" js/document.body.id)
               (comparison/initialize-page data (get-params))))
           (ensure-comparison-data k locale)))

    (when-let [selects (dom/qsa ".mvt-rda-selector")]
      (ensure-rda-data
       (str "rda-data-" (name locale))
       locale
       #(rda/initialize-rda-selectors (js->clj %) selects event-bus)))

    (sidebar/initialize ".mvt-sidebar-toggle"))

  (let [panel (js/document.getElementById "filter-panel")
        table (js/document.getElementById "filtered-table")]
    (when (and panel table)
      (filters/initialize-filter panel table)))

  (let [filter-panel (js/document.getElementById "filter-panel")
        mother-of-all-tables (js/document.getElementById "filtered-table")]
    (when (and filter-panel mother-of-all-tables)
      (table/init-giant-table filter-panel mother-of-all-tables)))

  (toggler/init)

  (hoverable/set-up js/document))

(defonce ^:export kicking-out-the-jams (boot))
