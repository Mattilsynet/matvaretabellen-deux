(ns matvaretabellen.ingest
  (:require [broch.core :as b]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.db :as db]))

(defn load-json [file-name]
  (-> (io/file file-name)
      slurp
      json/read-str))

(defn get-words [s & [re]]
  (str/split (str/trim s) (or re #" +")))

(def known-non-constituents
  ["Netto"
   "Energi1"
   "Energi2"
   "Portion"])

(defn parse-doublish [x]
  (when-not (#{"" "M" nil} x)
    (parse-double x)))

(defn get-constituents [food]
  (set
   (for [[id {:strs [ref value]}] (->> (apply dissoc food known-non-constituents)
                                       (filter (fn [[k v]] (get v "ref"))))]
     (let [grams (parse-double value)]
       (cond-> {:constituent/nutrient [:nutrient/id id]
                :measurement/source [:source/id ref]}
         grams (assoc :measurement/quantity (b/grams grams)))))))

(defn get-portions [{:strs [ref value]}]
  (->> (map (fn [r v]
              (try
                (when-let [grams (parse-doublish v)]
                  {:portion/kind [:portion-kind/id (keyword r)]
                   :portion/quantity (b/grams grams)})
                (catch Exception e
                  (throw (ex-info "Can't get me no portions" {:ref r :value v} e)))))
            (get-words ref)
            (get-words value))
       (remove nil?)
       set))

(defn get-energy [{:strs [ref value]}]
  (try
    (when-let [joules (parse-doublish value)]
      {:measurement/quantity (b/joules joules)
       :measurement/source [:origin/id ref]})
    (catch Exception e
      (throw (ex-info "Can't get me no energy" {:ref ref :value value} e)))))

(defn get-edible-part [{:strs [ref value]}]
  (try
    (when-let [pct (parse-doublish value)]
      {:measurement/percent (Math/round pct)
       :measurement/source [:origin/id ref]})
    (catch Exception e
      (throw (ex-info "Can't get me no edible part" {:ref ref :value value} e)))))

(defn foodcase-food->food [{:strs [id name slug groupId synonym latinName Netto
                                   langualCodes Vann Energi1 Energi2 Portion] :as food}]
  (->> {:food/id id
        :food/name name
        :food/slug slug
        :food/food-group [:food-group/id groupId]
        :food/search-keywords (set (get-words synonym #";"))
        :food/latin-name latinName
        :food/langual-codes (set (for [id (get-words langualCodes)]
                                   [:langual-code/id id]))
        :food/energy (get-energy Energi1)
        :food/calories {:measurement/observation (get Energi2 "value")
                        :measurement/source [:origin/id (get Energi2 "ref")]}
        :food/constituents (get-constituents food)

        :food/portions (get-portions Portion)
        :food/edible-part (get-edible-part Netto)}
       (remove (comp nil? second))
       (into {})))

(defn prepare-foodcase-foods [file-name]
  (->> (get (load-json file-name) "foods")
       (map foodcase-food->food)))

(defn strip-i18n-attrs [db data]
  (let [attrs (db/get-i18n-attrs db)]
    (walk/postwalk
     (fn [x]
       (if (map? x)
         (apply dissoc x attrs)
         x))
     data)))

(comment

  (load-json "foodcase-food-nb.json")

  (def food-nb (prepare-foodcase-foods "foodcase-food-nb.json"))
  (def food-en (prepare-foodcase-foods "foodcase-food.json"))
  (def schema (read-string (slurp (clojure.java.io/resource "db-schema.edn"))))
  (d/delete-database "datomic:mem://lol")
  (def conn (db/create-database "datomic:mem://lol" schema))

  (def food-nb-m (into {} (map (juxt :food/id identity) food-nb)))
  (def food-en-m (into {} (map (juxt :food/id identity) food-en)))

  (->> food-nb
       (filter (comp #{#{"kveitekli"}} :food/search-keywords))
       )

  (get (strip-i18n-attrs (d/db conn) food-nb-m) "05.494")
  (get (strip-i18n-attrs (d/db conn) food-en-m) "05.494")

  (=
   (strip-i18n-attrs (d/db conn) food-nb)
   (strip-i18n-attrs (d/db conn) food-en))

)
