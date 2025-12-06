(ns matvaretabellen.ui.search
  (:require [clojure.string :as str]
            [mattilsynet.design :as mtds]
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

(defn handle-autocomplete-input-event [e locale empty-html]
  (let [q (.-value (.-control (.-currentTarget e)))
        n (or (some-> (.-currentTarget e) (.getAttribute "data-suggestions") js/parseInt) 10)
        datalist (.-list (.-currentTarget e))]
    (if (< (.-length q) 2)
      (set! (.-innerHTML datalist) empty-html)
      (if (waiting?)
        (do (set! (.-innerHTML datalist) (str "<u-option><span class='" (str/join " " (mtds/classes :spinner)) "'></span></u-option>"))
            (add-watch search-engine ::waiting-for-load
                       #(when-not (waiting?)
                          (remove-watch search-engine ::waiting-for-load)
                          (handle-autocomplete-input-event e locale empty-html))))
        (set! (.-innerHTML datalist)
              (str/join
               (flatten
                (for [{:keys [url text]} (take n (search @search-engine q locale))]
                  ["<u-option value='" url "'>"
                   text
                   "</u-option>"]))))))))

(defn handle-autocomplete-submit-event [e]
  (when-let [q-or-url (some-> e .-detail .-value)]
    (.preventDefault e)
    (if (= q-or-url (.-value (.-control (.-currentTarget e))))
      ;;(.submit (.-form (.-control (.-currentTarget e))))
      (some-> (.-currentTarget e) .-control (.closest "form") .submit)
      (set! js/window.location q-or-url))))

(defn initialize-foods-autocomplete [dom-element locale initial-query]
  (when-let [combobox (dom/qs dom-element "u-combobox")]
    (let [empty-html (.-innerHTML (.-list combobox))]
      (.addEventListener combobox "input" #(handle-autocomplete-input-event % locale empty-html)))

    (when (and initial-query (some-> (.-control combobox) .-value empty?))
      (set! (.-value (.-control combobox)) initial-query))

    (.addEventListener combobox "comboboxbeforeselect" #(handle-autocomplete-submit-event %))))

(comment
  (reset! search-engine {:index-status :pending
                         :names-status :pending})

  (select-keys @search-engine [:names-status :index-status])

  (search @search-engine "laks" :nb)

  (get-in @search-engine [:index "foodNameEdgegrams" "eple" "11.076"])

  )
