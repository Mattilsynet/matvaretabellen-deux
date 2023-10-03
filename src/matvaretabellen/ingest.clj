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
  (->> (str/split (str/trim s) (or re #" +"))
       (remove empty?)))

(def known-non-constituents
  #{"Netto"
    "Energi1"
    "Energi2"
    "Portion"})

(defn parse-doublish [x]
  (when-not (#{"" "M" nil} x)
    (parse-double x)))

(defn get-constituents [food]
  (set
   (for [[id {:strs [ref value]}] (->> (apply dissoc food known-non-constituents)
                                       (filter (fn [[_ v]] (get v "ref"))))]
     (let [grams (parse-double value)]
       (cond-> {:constituent/nutrient [:nutrient/id id]
                :measurement/origin [:origin/id ref]}
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
       :measurement/origin [:origin/id ref]})
    (catch Exception e
      (throw (ex-info "Can't get me no energy" {:ref ref :value value} e)))))

(defn get-edible-part [{:strs [ref value]}]
  (try
    (when-let [pct (parse-doublish value)]
      {:measurement/percent (Math/round pct)
       :measurement/origin [:origin/id ref]})
    (catch Exception e
      (throw (ex-info "Can't get me no edible part" {:ref ref :value value} e)))))

(defn foodcase-food->food [{:strs [id name groupId synonym latinName Netto
                                   langualCodes Energi1 Energi2 Portion] :as food}]
  (->> {:food/id id
        :food/name name
        :food/food-group [:food-group/id groupId]
        :food/search-keywords (set (get-words synonym #";"))
        :food/latin-name latinName
        :food/langual-codes (set (for [id (get-words langualCodes)]
                                   [:langual-code/id id]))
        :food/energy (get-energy Energi1)
        :food/calories {:measurement/observation (get Energi2 "value")
                        :measurement/origin [:origin/id (get Energi2 "ref")]}
        :food/constituents (get-constituents food)

        :food/portions (get-portions Portion)
        :food/edible-part (get-edible-part Netto)}
       (remove (comp nil? second))
       (into {})))

(defn strip-i18n-attrs [attrs data]
  (walk/postwalk
   (fn [x]
     (if (map? x)
       (apply dissoc x attrs)
       x))
   data))

(defn validate-i18n-combination [locale->ms i18n-attrs]
  (or (when-not (->> (vals locale->ms)
                     (map #(strip-i18n-attrs i18n-attrs %))
                     (apply =))
        {:explanation "Localized food sources are not all alike"})
      (when (->> (tree-seq coll? identity locale->ms)
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

(defn combine-i18n-sources [locale->ms i18n-attrs]
  (when-let [error (validate-i18n-combination locale->ms i18n-attrs)]
    (throw (ex-info (:explanation error) {})))
  (let [locales (keys locale->ms)]
    (for [[m :as siblings] (->> (vals locale->ms)
                                (apply map vector)
                                vectorize-seqs)]
      (reduce
       (fn [m path]
         (assoc-in m path
                   (update-vals (zipmap locales siblings)
                                #(get-in % path))))
       m
       (find-key-paths m i18n-attrs)))))

(defn foodcase-foodgroup->food-group [{:strs [id parentId name]}]
  (cond-> {:food-group/id id
           :food-group/name name}
    (seq parentId)
    (assoc :food-group/parent {:food-group/id parentId})))

(defn foodcase-nutrient->nutrient [{:strs [id name euroFIR euroFIRname unit decimals parentId]}]
  (when-not (known-non-constituents id)
    (cond-> {:nutrient/id id
             :nutrient/name name
             :nutrient/euro-fir-id euroFIR
             :nutrient/euro-fir-name euroFIRname
             :nutrient/unit unit
             :nutrient/decimal-precision (some-> decimals parse-long)}
      (seq parentId)
      (assoc :nutrient/parent {:nutrient/id parentId}))))

(defn foodcase-reference->origin [{:strs [id text]}]
  {:origin/id id
   :origin/description text})

(defn foodcase-langualcode->langual-code [{:strs [id text]}]
  {:langual-code/id id
   :langual-code/description text})

(defn foodcase-portiontype->portion-kind [{:strs [id name unit]}]
  {:portion-kind/id (keyword id)
   :portion-kind/name name
   :portion-kind/unit unit})

(defn create-foodcase-transactions [db locale->datas]
  (let [i18n-attrs (db/get-i18n-attrs db)]
    [ ;; food-groups
     (combine-i18n-sources
      (update-vals locale->datas #(map foodcase-foodgroup->food-group (get % "foodgroups")))
      i18n-attrs)

     ;; nutrients
     (combine-i18n-sources
      (update-vals locale->datas #(keep foodcase-nutrient->nutrient (get % "nutrients")))
      i18n-attrs)

     ;; origins
     (combine-i18n-sources
      (update-vals locale->datas #(map foodcase-reference->origin (get % "references")))
      i18n-attrs)

     ;; langual-codes
     (map foodcase-langualcode->langual-code (get (first (vals locale->datas)) "langualcodes"))

     ;; portion-kinds
     (map foodcase-portiontype->portion-kind (get (first (vals locale->datas)) "portiontypes"))

     ;; foods
     (combine-i18n-sources
      (update-vals locale->datas #(map foodcase-food->food (get % "foods")))
      i18n-attrs)]))

(defn create-database-from-scratch [uri]
  (d/delete-database uri)
  (let [schema (read-string (slurp (io/resource "db-schema.edn")))
        conn (db/create-database uri schema)]
    (doseq [tx (create-foodcase-transactions
                (d/db conn)
                {:nb (merge (load-json "data/foodcase-data-nb.json")
                            (load-json "data/foodcase-food-nb.json"))
                 :en (merge (load-json "data/foodcase-data-en.json")
                            (load-json "data/foodcase-food-en.json"))})]
      @(d/transact conn tx))
    conn))

(comment
  (def conn (create-database-from-scratch "datomic:mem://matvaretabellen"))
  (def conn (d/connect "datomic:mem://matvaretabellen"))
  (def db (d/db conn))

  (d/touch (d/entity db [:food/id "05.421"]))

  )
