(ns matvaretabellen.ui.foods-search
  (:require [matvaretabellen.ui.query-engine :as qe]
            [matvaretabellen.urls :as urls]))

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

(defn search-foods [engine q]
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
                         (assoc :boost 5))
                     ;; Add fuzziness
                     (-> (:foodNameNgrams (:schema engine))
                         (merge {:q q
                                 :fields ["foodNameNgrams"]
                                 :operator :or
                                 :min-accuracy 0.8
                                 }))]
           :operator :or}))]
    (assoc match :name (get (:names engine) (:id match)))))

(defn search [engine q locale]
  (->> (concat
        (->> (for [result (search-nutrients engine q)]
               {:text (:name result)
                :url (urls/get-nutrient-url locale (:name result))})
             (take 3))
        (for [result (search-foods engine q)]
          {:text (:name result)
           :url (urls/get-food-url locale (:name result))}))))
