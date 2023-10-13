(ns ^:figwheel-hooks matvaretabellen.ui.main
  (:require [clojure.string :as str]
            [matvaretabellen.search :as search]
            [matvaretabellen.ui.foods-search :as foods-search]))

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

(defn handle-autocomplete-input-event [e element]
  (let [q (.-value (.-target e))]
    (if (< (.-length q) 3)
      (set! (.-innerHTML element) "")
      (set! (.-innerHTML element)
            (str/join
             (flatten
              ["<ol class='mvt-ac-results'>"
               (for [result (take 10 (foods-search/search @search-engine q))]
                 ["<li class='mvt-ac-result'>"
                  ["<a href=''>" (:name result) "</a>"]
                  "</li>"])
               "</ol>"]))))))

(defn initialize-foods-autocomplete [dom-element]
  (let [element (js/document.createElement "div")]
    (.appendChild dom-element element)
    (.addEventListener dom-element "input" #(handle-autocomplete-input-event % element))))

(defn ^:after-load main []
  (populate-search-engine js/document.documentElement.lang))

(defn boot []
  (main)
  (initialize-foods-autocomplete (js/document.querySelector ".mvt-autocomplete")))

(defonce ^:export kicking-out-the-jams (boot))

(comment
  (reset! search-engine {:index-status :pending
                         :foods-status :pending})

  (main)
  (select-keys @search-engine [:foods-status :index-status])

  (foods-search/search @search-engine "laks")

  (get-in @search-engine [:index "foodNameEdgegrams" "eple" "11.076"])

  )
