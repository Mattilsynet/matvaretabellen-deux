(ns matvaretabellen.ui.search
  (:require [clojure.string :as str]
            [matvaretabellen.search :as search]
            [matvaretabellen.ui.query-engine :as qe]
            [matvaretabellen.urls :as urls]))

(defonce search-engine (atom {:index-status :pending
                              :names-status :pending}))

(defn lookup-food [{:keys [names]} q]
  (when (get names q)
    [{:id q}]))

(defn search-nutrients [engine q]
  (for [match (qe/query
               (:index engine)
               {:queries [ ;; "Autocomplete" what the user is typing
                          (-> (:nutrientNameEdgegrams (:schema engine))
                              (assoc :q q)
                              (assoc :fields ["nutrientNameEdgegrams"]))]})]
    (assoc match :name (get (:names engine) (:id match)))))

(defn search-foods
  ([q] (search-foods @search-engine q))
  ([engine q]
   (for [match
         (concat
          (lookup-food engine q)
          (qe/query
           (:index engine)
           {:queries [;; "Autocomplete" what the user is typing
                      (-> (:foodNameEdgegrams (:schema engine))
                          (assoc :q q)
                          (assoc :fields ["foodNameEdgegrams"])
                          (assoc :boost 10))
                      ;; Boost exact matches
                      (-> (:foodName (:schema engine))
                          (assoc :q q)
                          (assoc :fields ["foodName" "foodId"])
                          (assoc :boost 10))
                      ;; Add fuzziness
                      (-> (:foodNameNgrams (:schema engine))
                          (merge {:q q
                                  :fields ["foodNameNgrams"]
                                  :operator :or
                                  :min-accuracy 0.8}))]
            :operator :or}))]
     (assoc match :name (get (:names engine) (:id match))))))

(defn search [engine q locale]
  (->> (concat
        (->> (for [result (search-nutrients engine q)]
               {:text (:name result)
                :url (urls/get-nutrient-url locale (:name result))})
             (take 3))
        (for [result (search-foods engine q)]
          {:id (:id result)
           :text (:name result)
           :url (urls/get-food-url locale (:name result))}))))

(defn load-json [url]
  (-> (js/fetch url)
      (.then #(.text %))
      (.then #(js->clj (js/JSON.parse %)))))

(defn populate-search-engine [locale]
  (when-not (:schema @search-engine)
    (swap! search-engine assoc :schema (search/create-schema (keyword locale))))
  (when (#{:pending :error} (:index-status @search-engine))
    (swap! search-engine assoc :index-status :loading)
    (-> (load-json (str "/search/index/" locale ".json"))
        (.then #(swap! search-engine assoc
                       :index %
                       :index-status :ready))
        (.catch (fn [e]
                  (js/console.error e)
                  (swap! search-engine assoc :index-status :error)))))
  (when (#{:pending :error} (:names-status @search-engine))
    (swap! search-engine assoc :names-status :loading)
    (-> (load-json (str "/search/names/" locale ".json"))
        (.then #(swap! search-engine assoc
                       :names %
                       :names-status :ready))
        (.catch (fn [e]
                  (js/console.error e)
                  (swap! search-engine assoc :names-status :error))))))

(defn waiting? []
  (or (#{:pending :loading} (:index-status @search-engine))
      (#{:pending :loading} (:names-status @search-engine))))

(defn handle-autocomplete-input-event [e element locale]
  (let [q (.-value (.-target e))
        n (or (some-> (.-target e) (.getAttribute "data-suggestions") js/parseInt) 10)]
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
                 (for [{:keys [url text]} (take n (search @search-engine q locale))]
                   ["<li class='mmm-ac-result'>"
                    ["<a href='" url "'>" text "</a>"]
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
  (when-let [input (some-> dom-element (.querySelector "#foods-search"))]
    (let [element (js/document.createElement "div")]
      (.appendChild dom-element element)
      (.addEventListener dom-element "input" #(handle-autocomplete-input-event % element locale))
      (.addEventListener dom-element "keyup" #(handle-autocomplete-key-event % element))
      (when-let [form (.closest dom-element "form")]
        (.addEventListener form "submit" #(handle-autocomplete-submit-event %)))
      (when (and initial-query (empty? (.-value input)))
        (set! (.-value input) initial-query))
      (when (seq (.-value input))
        (handle-autocomplete-input-event #js {:target input} element locale)))))

(comment
  (reset! search-engine {:index-status :pending
                         :names-status :pending})

  (select-keys @search-engine [:names-status :index-status])

  (search @search-engine "laks" :nb)

  (get-in @search-engine [:index "foodNameEdgegrams" "eple" "11.076"])

  )
