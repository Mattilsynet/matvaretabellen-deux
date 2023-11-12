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

(defn qsa [selector]
  (seq (js/document.querySelectorAll selector)))

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

(defn calc-new-portion-fraction [portion-size per-100g & [{:keys [decimals]}]]
  (let [orig-decimals (count-decimals per-100g)]
    (str/replace
     (.toFixed
      ;; Avoid 0.234 being rounded to 0 when using 0 decimals (kcal, kJ)
      (js/Math.max 1 (* portion-size (/ per-100g 100.0)))

      ;; Max 2 extra decimals if no decimals where specified
      (or decimals (+ 2 orig-decimals)))
     #"\.?0+$" "")))

(defn handle-portion-select-event [e portion-elements portion-label-elements]
  (let [value (.-value (.-target e))
        label (some->> (.-options (.-target e))
                       into-array
                       (filter #(= value (.-value %)))
                       first
                       .-innerText)
        portion-size (js/Number. (.-value (.-target e)))]
    (doseq [elem (seq portion-label-elements)]
      (set! (.-innerHTML elem) label))
    (doseq [elem (seq portion-elements)]
      (set! (.-innerHTML elem)
            (calc-new-portion-fraction
             portion-size
             (js/Number. (.getAttribute elem "data-portion"))
             (when-let [decimals (.getAttribute elem "data-decimals")]
               {:decimals (js/Number. decimals)}))))))

(defn initialize-portion-selector [select-element portion-elements portion-label-elements]
  (when select-element
    (.addEventListener
     select-element
     "change"
     #(handle-portion-select-event % portion-elements portion-label-elements))))

;; There's a script tag at the beginning of the body tag that sets the
;; mvt-source-hide class immediately to avoid any flickering. Please keep it in
;; mind if you change this.
(defn toggle-sources [_e selector]
  (let [show? (boolean (js/document.body.classList.contains "mvt-source-hide"))]
    (doseq [checkbox (qsa selector)]
      (set! (.-checked checkbox) show?))
    (js/localStorage.setItem "show-sources" show?)
    (if show?
      (js/document.body.classList.remove "mvt-source-hide")
      (js/document.body.classList.add "mvt-source-hide"))))

(defn initialize-source-toggler [selector]
  (let [showing? (= "true" (js/localStorage.getItem "show-sources"))]
    (when-not showing?
      (js/document.body.classList.add "mvt-source-hide"))
    (doseq [checkbox (qsa selector)]
      (when showing?
        (set! (.-checked checkbox) true))
      (.addEventListener checkbox "input" #(toggle-sources % selector)))))

(defn set-energy [el food]
  (let [kj (.querySelector el ".mvt-kj")
        kcal (.querySelector el ".mvt-kcal")]
    (set! (.-innerHTML kj) (:energyKj food))
    (.setAttribute kj "data-portion" (:energyKj food))
    (set! (.-innerHTML kcal) (:energyKcal food))
    (.setAttribute kcal "data-portion" (:energyKcal food))))

(defn set-nutrient-content [el food]
  (let [[n sym] (some-> (:constituents food)
                        (get (keyword (.getAttribute el "data-nutrient-id")))
                        :quantity)
        num-el (.querySelector el "[data-portion]")]
    (set! (.-innerHTML num-el) n)
    (.setAttribute num-el "data-portion" n)
    (set! (.-innerHTML (.querySelector el ".mvt-sym")) sym)))

(defn prepare-comparison-el [el food]
  (or
   (when (.contains (.-classList el) "mvtc-food-name")
     (set! (.-innerHTML el) (str "<a class=\"mmm-link\" href=\"" (:url food) "\">" (:foodName food) "</a>")))

   (when (.contains (.-classList el) "mvtc-energy")
     (set-energy el food))

   (when-let [edible (.querySelector el ".mvtc-edible-part")]
     (set! (.-innerHTML edible) (or (:ediblePart food) "0")))

   (when (.contains (.-classList el) "mvtc-nutrient")
     (set-nutrient-content el food))))

(def comparison-k "comparisonFoods")

(defn get-foods-to-compare []
  (some-> (js/localStorage.getItem comparison-k)
          not-empty
          js/JSON.parse
          (js->clj :keywordize-keys true)))

(defn initialize-comparison []
  (when-let [foods (get-foods-to-compare)]
    (when (< 4 (count foods))
      (let [container (js/document.getElementById "container")]
        (.remove (.-classList container) "mmm-container-focused")
        (.add (.-classList container) "mmm-container")))
    (doseq [row (qsa ".mvtc-comparison")]
      (let [template (.-lastChild row)]
        (doseq [_food (next foods)]
          (.appendChild row (.cloneNode template true))))
      (doseq [[el food] (map vector (next (seq (.-childNodes row))) foods)]
        (prepare-comparison-el el food)))))

(defn boot []
  (main)
  (initialize-foods-autocomplete
   (js/document.querySelector ".mmm-search-input")
   (keyword js/document.documentElement.lang)
   (get (get-params) "search"))

  (initialize-portion-selector
   (js/document.querySelector "#portion-selector")
   (js/document.querySelectorAll "[data-portion]")
   (js/document.querySelectorAll ".js-portion-label"))

  (initialize-source-toggler ".mvt-source-toggler input")

  (when (= "comparison" js/document.body.id)
    (initialize-comparison))

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
