(ns matvaretabellen.ui.rda
  "RDA - Recommended Daily Allowance."
  (:require [matvaretabellen.ui.dom :as dom]
            [matvaretabellen.ui.portions :as portions]))

(defn get-recommended-amount [profile nutrient-id]
  (let [recommendation (get-in profile ["recommendations" nutrient-id])]
    (or (get-in recommendation ["maxAmount" 0])
        (get-in recommendation ["minAmount" 0])
        (get-in recommendation ["averageAmount" 0]))))

(defn update-rda-values [profile]
  (doseq [el (dom/qsa ".mvt-rda")]
    (let [nutrient-id (.getAttribute el "data-nutrient-id")]
      (set! (.-innerHTML el)
            (if-let [recommended-amount (get-recommended-amount profile nutrient-id)]
              (-> (.closest el "tr")
                  (.querySelector "[data-portion]")
                  (.getAttribute "data-value")
                  js/parseFloat
                  (/ recommended-amount)
                  (* 100)
                  (.toFixed 0)
                  (str " %"))
              (do
                (println "No recommended amount for" nutrient-id)
                (prn profile)
                ""))))))

(defn select-profile [selects profile-data]
  (update-rda-values profile-data)
  (doseq [s selects]
    (set! (.-value s) (get profile-data "id"))))

(defn initialize-rda-selectors [data selects event-bus]
  (when-let [profile (dom/get-local-edn "selected-rda-profile")]
    (select-profile selects profile))
  (->> (fn [_ _ _ event]
         (when (= ::portions/changed-portions event)
           (update-rda-values (get data (.-value (first selects))))))
       (add-watch event-bus ::rda))
  (doseq [select selects]
    (->> (fn [_e]
           (dom/set-local-edn "selected-rda-profile" (get data (.-value select)))
           (select-profile
            (remove #{select} selects)
            (get data (.-value select))))
         (.addEventListener select "input"))))
