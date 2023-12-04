(ns matvaretabellen.pages.api
  (:require [broch.core :as b]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.food :as food]
            [matvaretabellen.nutrient :as nutrient]
            [matvaretabellen.urls :as urls]))

(defn get-all-foods [context page]
  (->> (d/q '[:find [?f ...]
              :where
              [?f :food/id]]
            (:foods/db context))
       (map #(d/entity (:foods/db context) %))
       (sort-by (comp (:page/locale page) :food/name))))

(defn camel-case-k [s]
  (let [[w & ws] (str/split s #"-")]
    (keyword (str w (str/join (map str/capitalize ws))))))

(def bespoke-json-keys
  {:food/id :foodId
   :food/name :foodName
   :food-group/id :foodGroupId
   :langual-code/id :langualCode
   :portion-kind/name :portionName
   :portion-kind/unit :portionUnit
   :quantity/number :quantity
   :nutrient/id :nutrientId
   :source/id :sourceId})

(defn ->json [data]
  (walk/postwalk
   (fn [x]
     (if (keyword? x)
       (or (bespoke-json-keys x)
           (camel-case-k (name x)))
       x))
   data))

(defn prepare-response [context page data]
  (let [base-url (-> context :powerpack/app :site/base-url)]
    (cond-> (walk/postwalk
             (fn [x]
               (cond-> x
                 (:page/uri x) (update :page/uri #(str base-url %))))
             data)
      (= :json (:page/format page)) ->json)))

(defn food->compact-api-data [locale food]
  {:id (:food/id food)
   :url (urls/get-food-url locale food)
   :foodName (get (:food/name food) locale)
   :energyKj (some-> food :food/energy :measurement/quantity b/num int)
   :energyKcal (some-> food :food/calories :measurement/observation parse-long)
   :ediblePart (:measurement/percent (:food/edible-part food))
   :constituents (->> (for [constituent (:food/constituents food)]
                        [(-> constituent :constituent/nutrient :nutrient/id)
                         {:quantity [(or (some-> constituent :measurement/quantity b/num) 0)
                                     (or (some-> constituent :measurement/quantity b/symbol) "g")]}])
                      (into {}))})

(defn render-compact-foods [context page]
  {:content-type :json
   :body (->> (get-all-foods context page)
              (map #(food->compact-api-data (:page/locale page) %)))})

(defn ->api-quantity [quantity]
  {:quantity/number (b/num quantity)
   :quantity/unit (b/symbol quantity)})

(defn ->api-measurement [e & [observation-unit]]
  (cond-> (select-keys (into {} e) [:measurement/percent])
    (:measurement/source e)
    (assoc :source/id (:source/id (:measurement/source e)))

    (:measurement/quantity e)
    (merge (->api-quantity (:measurement/quantity e)))

    (:measurement/observation e)
    (merge {:quantity/number (parse-long (:measurement/observation e))
            :quantity/unit observation-unit})

    (:constituent/nutrient e)
    (assoc :nutrient/id (-> e :constituent/nutrient :nutrient/id))))

(defn ->api-portion [locale portion]
  (merge
   {:portion-kind/name (get-in (:portion/kind portion) [:portion-kind/name locale])
    :portion-kind/unit (:portion-kind/unit (:portion/kind portion))}
   (->api-quantity (:portion/quantity portion))))

(defn food->api-data [locale food]
  (-> (into {:page/uri (urls/get-food-url locale food)} food)
      (update :food/name locale)
      (dissoc :food/food-group)
      (assoc :food-group/id (-> food :food/food-group :food-group/id))
      (update :food/search-keywords locale)
      (update :food/langual-codes #(set (map :langual-code/id %)))
      (update :food/edible-part ->api-measurement)
      (update :food/energy ->api-measurement)
      (update :food/calories ->api-measurement "kcal")
      (update :food/portions #(set (map (partial ->api-portion locale) %)))
      (update :food/constituents #(set (map ->api-measurement %)))))

(defn render-food-data [context page]
  {:content-type (:page/format page)
   :body {:foods (->> (get-all-foods context page)
                      (map #(food->api-data (:page/locale page) %))
                      (prepare-response context page))
          :locale (:page/locale page)}})

(defn nutrient->api-data [locale nutrient]
  (-> (cond-> {:page/uri (urls/get-nutrient-url locale nutrient)}
        (:nutrient/parent nutrient)
        (assoc :nutrient/parent-id (-> nutrient :nutrient/parent :nutrient/id)))
      (into nutrient)
      (update :nutrient/name locale)
      (dissoc :nutrient/parent)))

(defn render-nutrient-data [context page]
  {:content-type (:page/format page)
   :body {:nutrients (->> (nutrient/get-used-nutrients (:foods/db context))
                          nutrient/sort-by-preference
                          (map #(nutrient->api-data (:page/locale page) %))
                          (prepare-response context page))
          :locale (:page/locale page)}})

(defn render-langual-data [context page]
  {:content-type (:page/format page)
   :body {:codes (->> (d/q '[:find [?e ...]
                             :where
                             [?e :langual-code/id]]
                           (:foods/db context))
                      (map #(d/entity (:foods/db context) %))
                      (sort-by :langual-code/id)
                      (map #(-> (into {} %)
                                (update :langual-code/description food/humanize-langual-classification)))
                      (prepare-response context page))}})
