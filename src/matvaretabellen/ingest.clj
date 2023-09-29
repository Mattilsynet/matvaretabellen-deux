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

(defn strip-i18n-attrs [attrs data]
  (walk/postwalk
   (fn [x]
     (if (map? x)
       (apply dissoc x attrs)
       x))
   data))

(defn validate-food-sources [i18n-attrs locale->foods]
  (or (when-not (->> (vals locale->foods)
                     (map #(strip-i18n-attrs i18n-attrs %))
                     (apply =))
        {:explanation "Localized food sources are not all alike"})
      (when (->> (tree-seq coll? identity locale->foods)
                 (filter set?)
                 (tree-seq coll? identity)
                 (filter i18n-attrs)
                 seq)
        {:explanation "Can't use i18n attributes inside a set"})))

(defn vectorize-seqs [form]
  (walk/postwalk
   (fn [x]
     (cond-> x
       (seq? x) vec))
   form))

(defn find-key-paths
  ([form ks] (find-key-paths form ks []))
  ([form ks path]
   (cond
     (map-entry? form)
     (cond->> (find-key-paths (val form) ks path)
       (ks (key form))
       (cons path))

     (map? form)
     (mapcat (fn [kv]
               (find-key-paths kv ks (conj path (key kv))))
             form)

     (vector? form)
     (mapcat (fn [i v] (find-key-paths v ks (conj path i)))
             (range)
             form))))

(defn combine-i18n-sources [locale->foods i18n-attrs]
  (let [locales (keys locale->foods)]
    (for [[food :as siblings] (->> (vals locale->foods)
                                   (apply map vector)
                                   vectorize-seqs)]
      (reduce
       (fn [food path]
         (assoc-in food path
                   (update-vals (zipmap locales siblings)
                                #(get-in % path))))
       food
       (find-key-paths food i18n-attrs)))))

(comment

  (load-json "foodcase-food-nb.json")

  (def food-nb (prepare-foodcase-foods "foodcase-food-nb.json"))
  (def food-en (prepare-foodcase-foods "foodcase-food.json"))
  (def schema (read-string (slurp (clojure.java.io/resource "db-schema.edn"))))
  (d/delete-database "datomic:mem://lol")
  (def conn (db/create-database "datomic:mem://lol" schema))
  (def db (d/db conn))

  (db/get-i18n-attrs (d/db conn))

  (combine-i18n-sources
   (d/db conn)
   {:nb (take 10 food-nb)
    :en (take 10 food-en)})

  (first food-en)

  (find-attr-paths {:en (take 1 food-en)} (db/get-i18n-attrs db))

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
