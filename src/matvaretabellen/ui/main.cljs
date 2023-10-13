(ns ^:figwheel-hooks matvaretabellen.ui.main
  (:require [matvaretabellen.ui.foods-search :as foods-search]
            [matvaretabellen.search :as search]))

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

(defn ^:after-load main []
  (populate-search-engine js/document.documentElement.lang))

(defn ^:export boot []
  (main))

(comment

  (reset! search-engine {:index-status :pending
                         :foods-status :pending})

  (main)
  (select-keys @search-engine [:foods-status :index-status])

  (foods-search/search @search-engine "eple")

  (get-in @search-engine [:index "foodNameEdgegrams" "eple" "11.076"])

  )
