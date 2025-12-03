(ns matvaretabellen.ui.search
  (:require [clojure.string :as str]
            [matvaretabellen.search :as search]
            [matvaretabellen.ui.dom :as dom]
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

(defn on-ready [f]
  (if (waiting?)
    (let [id (random-uuid)]
      (add-watch search-engine id (fn [_ _ _ _]
                                    (when-not (waiting?)
                                      (f)
                                      (remove-watch search-engine id)))))
    (f)))

(defn handle-autocomplete-input-event [e input element locale]
  (if-let [url (when (and (not (.-inputType e)) (.-type e))
                 (some-> e .-target .-value))]
    (do
      (set! (.-value input) "")
      (set! (.. js/window -location -href) url))
    (let [q (.-value (.-target e))
          n (or (some-> (.-target e) (.getAttribute "data-suggestions") js/parseInt) 10)]
      (if (< (.-length q) 3)
        (do
          (set! (.-innerHTML element) "")
          (dom/hide element))
        (do
          (dom/show element)
          (if (waiting?)
            (do (set! (.-innerHTML element) "<u-option class='mmm-ac-result tac'><span class='mmm-loader'></span></u-option>")
                (add-watch search-engine ::waiting-for-load
                           #(when-not (waiting?)
                              (remove-watch search-engine ::waiting-for-load)
                              (handle-autocomplete-input-event e input element locale))))
            (set! (.-innerHTML element)
                  (str/join
                   (flatten
                    (for [{:keys [url text]} (take n (search @search-engine q locale))]
                      ["<u-option class='mmm-ac-result' value='" url "'>"
                       text
                       "</u-option>"]))))))))))

(defn handle-autocomplete-submit-event [e]
  (when-let [selected (.querySelector (.-target e) ".mmm-ac-selected a")]
    (.preventDefault e)
    (set! js/window.location (.-href selected))))

(defn initialize-foods-autocomplete [dom-element locale initial-query]
  (when-let [input (dom/qs dom-element "input")]
    (let [element (dom/qs dom-element ".mmm-ac-results")]
      (.addEventListener dom-element "input" #(handle-autocomplete-input-event % input element locale))
      (when-let [form (.closest dom-element "u-combobox")]
        (.addEventListener form "comboboxbeforeselect" #(handle-autocomplete-submit-event %)))
      (when (and initial-query (some-> input .-value empty?))
        (set! (.-value input) initial-query))
      (when (seq (.-value input))
        (handle-autocomplete-input-event #js {:target input} input element locale)
        (js/requestAnimationFrame #(.focus input))))))

(comment
  (reset! search-engine {:index-status :pending
                         :names-status :pending})

  (select-keys @search-engine [:names-status :index-status])

  (search @search-engine "laks" :nb)

  (get-in @search-engine [:index "foodNameEdgegrams" "eple" "11.076"])

  )
