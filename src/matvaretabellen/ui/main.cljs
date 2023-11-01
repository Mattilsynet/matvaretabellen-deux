(ns ^:figwheel-hooks matvaretabellen.ui.main
  (:require [clojure.string :as str]
            [matvaretabellen.search :as search]
            [matvaretabellen.ui.foods-search :as foods-search]
            [matvaretabellen.ui.hoverable :as hoverable]
            [matvaretabellen.urls :as urls]))

(defonce search-engine (atom {:index-status :pending
                              :foods-status :pending}))

(defn load-json [url]
  (-> (js/fetch url)
      (.then #(.text %))
      (.then #(js->clj (js/JSON.parse %)))))

(defn populate-search-engine [locale]
  (when-not (:schema @search-engine)
    (swap! search-engine assoc :schema (search/create-schema (keyword locale))))
  (when (#{:pending :error} (:index-status @search-engine))
    (swap! search-engine assoc :index-status :loading)
    (-> (load-json (str "/index/" locale ".json"))
        (.then #(swap! search-engine assoc
                       :index %
                       :index-status :ready))
        (.catch (fn [e]
                  (js/console.error e)
                  (swap! search-engine assoc :index-status :error)))))
  (when (#{:pending :error} (:foods-status @search-engine))
    (swap! search-engine assoc :foods-status :loading)
    (-> (load-json (str "/foods/" locale ".json"))
        (.then #(swap! search-engine assoc
                       :foods %
                       :foods-status :ready))
        (.catch (fn [e]
                  (js/console.error e)
                  (swap! search-engine assoc :foods-status :error))))))

(defn waiting? []
  (or (#{:pending :loading} (:index-status @search-engine))
      (#{:pending :loading} (:foods-status @search-engine))))

(defn handle-autocomplete-input-event [e element locale]
  (let [q (.-value (.-target e))]
    (if (< (.-length q) 3)
      (set! (.-innerHTML element) "")
      (if (waiting?)
        (do (set! (.-innerHTML element) "<ol class='mmm-ac-results'><li class='mmm-ac-result tac'><span class='mmm-loader'></span></li></ol>")
            (add-watch search-engine ::waiting-for-load
                       #(when-not (waiting?)
                          (remove-watch search-engine ::waiting-for-load)
                          (handle-autocomplete-input-event e element locale))))
        (set! (.-innerHTML element)
              (str/join
               (flatten
                ["<ol class='mmm-ac-results'>"
                 (for [result (take 10 (foods-search/search @search-engine q))]
                   ["<li class='mmm-ac-result'>"
                    ["<a href='" (urls/get-food-url locale (:name result)) "'>" (:name result) "</a>"]
                    "</li>"])
                 "</ol>"])))))))

(defn get-target-element [results selected d]
  (when (< 0 (.-length results))
    (cond
      (and selected (= :down d))
      (.-nextSibling selected)

      (and selected (= :up d))
      (.-previousSibling selected)

      (= :down d)
      (aget results 0)

      (= :up d)
      (aget results (dec (.-length results))))))

(defn navigate-results [element d]
  (let [selected (.querySelector element ".mmm-ac-selected")
        target-element (get-target-element (.querySelectorAll element ".mmm-ac-result") selected d)]
    (when target-element
      (when selected
        (.remove (.-classList selected) "mmm-ac-selected"))
      (.add (.-classList target-element) "mmm-ac-selected"))))

(defn handle-autocomplete-key-event [e element]
  (case (.-key e)
    "ArrowUp" (navigate-results element :up)
    "ArrowDown" (navigate-results element :down)
    nil))

(defn handle-autocomplete-submit-event [e]
  (.preventDefault e)
  (when-let [selected (.querySelector (.-target e) ".mmm-ac-selected a")]
    (set! js/window.location (.-href selected))))

(defn initialize-foods-autocomplete [dom-element locale initial-query]
  (when dom-element
    (let [input (.querySelector dom-element "#foods-search")
          element (js/document.createElement "div")]
      (.appendChild dom-element element)
      (.addEventListener dom-element "input" #(handle-autocomplete-input-event % element locale))
      (.addEventListener dom-element "keyup" #(handle-autocomplete-key-event % element))
      (.addEventListener (.closest dom-element "form") "submit" #(handle-autocomplete-submit-event %))
      (when (and initial-query (empty? (.-value input)))
        (set! (.-value input) initial-query))
      (when (seq (.-value input))
        (handle-autocomplete-input-event #js {:target input} element locale)))))

(defn ^:after-load main []
  (populate-search-engine js/document.documentElement.lang))

(defn get-params []
  (when (seq js/location.search)
    (update-vals (apply hash-map (str/split (subs js/location.search 1) #"[=&]"))
                 #(js/decodeURIComponent %))))

(defn count-decimals [n]
  (if (= (Math/floor n) n)
    0
    (count (second (str/split (str n) #"\.")))))

(defn calc-new-portion-fraction [portion-size per-100g]
  (let [orig-decimals (count-decimals per-100g)]
    (str/replace (.toFixed (* portion-size (/ per-100g 100.0))
                           (+ 2 orig-decimals)) ;; max 2 extra decimals
                 #"\.?0+$" "")))

(defn handle-portion-select-event [e portion-elements]
  (let [portion-size (js/Number. (.-value (.-target e)))]
    (doseq [elem (seq portion-elements)]
      (set! (.-innerHTML elem)
            (calc-new-portion-fraction portion-size
                                       (js/Number. (.getAttribute elem "data-portion")))))))

(defn initialize-portion-selector [select-element portion-elements]
  (when select-element
    (.addEventListener select-element "change" #(handle-portion-select-event % portion-elements))))

(defn boot []
  (main)
  (initialize-foods-autocomplete
   (js/document.querySelector ".mmm-search-input")
   (keyword js/document.documentElement.lang)
   (get (get-params) "search"))

  (initialize-portion-selector
   (js/document.querySelector "#portion-selector")
   (js/document.querySelectorAll "[data-portion]"))

  (hoverable/set-up js/document))

(defonce ^:export kicking-out-the-jams (boot))

(comment
  (reset! search-engine {:index-status :pending
                         :foods-status :pending})

  (main)
  (select-keys @search-engine [:foods-status :index-status])

  (foods-search/search @search-engine "laks")

  (get-in @search-engine [:index "foodNameEdgegrams" "eple" "11.076"])

  )
