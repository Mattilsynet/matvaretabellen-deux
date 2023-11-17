(ns ^:figwheel-hooks matvaretabellen.ui.main
  (:require [clojure.string :as str]
            [matvaretabellen.ui.comparison :as comparison]
            [matvaretabellen.ui.dom :as dom]
            [matvaretabellen.ui.food-group :as food-group]
            [matvaretabellen.ui.hoverable :as hoverable]
            [matvaretabellen.ui.portions :as portions]
            [matvaretabellen.ui.search :as search-ui]
            [matvaretabellen.ui.sidebar :as sidebar]
            [matvaretabellen.urls :as urls]))

(defn ^:after-load main []
  (search-ui/populate-search-engine js/document.documentElement.lang))

(defn get-params []
  (when (seq js/location.search)
    (update-vals (apply hash-map (str/split (subs js/location.search 1) #"[=&]"))
                 #(js/decodeURIComponent %))))

(defn update-source-toggler [el showing?]
  (if-let [checkbox (dom/qs el "input")]
    (set! (.-checked checkbox) showing?)
    (if showing?
      (.remove (.-classList el) "mmm-button-secondary")
      (.add (.-classList el) "mmm-button-secondary"))))

;; There's a script tag at the beginning of the body tag that sets the
;; mvt-source-hide class immediately to avoid any flickering. Please keep it in
;; mind if you change this.
(defn toggle-sources [_e selector]
  (let [show? (boolean (js/document.body.classList.contains "mvt-source-hide"))]
    (doseq [el (dom/qsa selector)]
      (update-source-toggler el show?))
    (js/localStorage.setItem "show-sources" show?)
    (if show?
      (js/document.body.classList.remove "mvt-source-hide")
      (js/document.body.classList.add "mvt-source-hide"))))

(defn initialize-source-toggler [selector]
  (let [showing? (= "true" (js/localStorage.getItem "show-sources"))]
    (when-not showing?
      (js/document.body.classList.add "mvt-source-hide"))
    (doseq [toggler (dom/qsa selector)]
      (update-source-toggler toggler showing?)
      (if-let [checkbox (dom/qs toggler "input")]
        (.addEventListener checkbox "input" #(toggle-sources % selector))
        (.addEventListener toggler "click" #(toggle-sources % selector))))))

(defn get-session-item [k]
  (try
    (some-> (js/sessionStorage.getItem k)
            js/JSON.parse)
    (catch :default _e
      nil)))

(defn set-session-item [k item]
  (try
    (js/sessionStorage.setItem k (js/JSON.stringify item))
    (catch :default _e)))

(defn get-local-item [k]
  (try
    (some-> (js/localStorage.getItem k)
            js/JSON.parse
            js->clj)
    (catch :default _e
      nil)))

(defn set-local-item [k item]
  (try
    (js/localStorage.setItem k (js/JSON.stringify (clj->js item)))
    (catch :default _e)))

(defn get-recommended-amount [profile nutrient-id]
  (let [recommendation (get-in profile ["recommendations" nutrient-id])]
    (or (get-in recommendation ["maxAmount" 0])
        (get-in recommendation ["minAmount" 0])
        (get-in recommendation ["averageAmount" 0]))))

(defn update-rda-values [profile]
  (doseq [el (dom/qsa ".mvt-rda")]
    (let [nutrient-id (.getAttribute el "data-nutrient-id")]
      (set! (.-innerHTML el)
            (-> (.closest el "tr")
                (.querySelector "[data-portion]")
                (.getAttribute "data-value")
                js/parseFloat
                (/ (get-recommended-amount profile nutrient-id))
                (* 100)
                (.toFixed 0)
                (str "&nbsp;%"))))))

(defn select-profile [selects profile-data]
  (update-rda-values profile-data)
  (doseq [s selects]
    (set! (.-value s) (get profile-data "id"))))

(defn initialize-rda-selectors [data selects event-bus]
  (when-let [profile (get-local-item "selected-rda-profile")]
    (select-profile selects (js->clj profile)))
  (->> (fn [_ _ _ event]
         (when (= ::changed-portions event)
           (update-rda-values (get data (.-value (first selects))))))
       (add-watch event-bus ::rda))
  (doseq [select selects]
    (->> (fn [_e]
           (set-local-item "selected-rda-profile" (get data (.-value select)))
           (select-profile
            (remove #{select} selects)
            (get data (.-value select))))
         (.addEventListener select "input"))))

(defn ensure-session-data [k url f & [{:keys [process-raw-data]}]]
  (try
    (if-let [data (get-session-item k)]
      (f data)
      (-> (js/fetch url)
          (.then (fn [res] (.json res)))
          (.then (fn [raw-data]
                   (let [data (cond-> raw-data
                                (ifn? process-raw-data) process-raw-data)]
                     (set-session-item k data)
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

    (initialize-source-toggler ".mvt-source-toggler")

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
       #(initialize-rda-selectors (js->clj %) selects event-bus)))

    (sidebar/initialize ".mvt-sidebar-toggle"))

  (let [panel (js/document.getElementById "filter-panel")
        table (js/document.getElementById "filtered-table")]
    (when (and panel table)
      (food-group/initialize-filter panel table)))

  (hoverable/set-up js/document))

(defonce ^:export kicking-out-the-jams (boot))
