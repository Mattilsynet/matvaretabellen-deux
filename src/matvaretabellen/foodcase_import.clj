(ns matvaretabellen.foodcase-import
  (:require [broch.core :as b]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.db :as db]
            [matvaretabellen.misc :as misc]
            [matvaretabellen.nutrient :as nutrient]))

(defn load-json [file-name]
  (-> (io/file file-name)
      slurp
      json/read-str))

(defn get-words [s & [re]]
  (->> (str/split (str/trim s) (or re #" +"))
       (remove empty?)))

(defn parse-portion-kinds [s]
  (loop [xs (seq (get-words s))
         res []]
    (cond
      (nil? xs)
      res

      (some-> (second xs) (str/starts-with? "("))
      (recur (next (next xs))
             (conj res (keyword
                        (str (first xs) "_"
                             (str/replace (second xs) #"[\(\)]" "")))))

      :else
      (recur (next xs) (conj res (keyword (first xs)))))))

(def nutrient-id->decimal-precision
  (read-string (slurp "data/nutrient-decimal-precision.edn")))

(def known-non-constituents
  #{"Netto"
    "Energi1"
    "Energi2"
    "Portion"})

(defn parse-doublish [x]
  (when-not (#{"" "M" nil} x)
    (parse-double x)))

(defn get-nutrient-quantity [nutrient-id constituent-txes]
  (->> constituent-txes
       ;; The constituent tx data holds a [:nutrient/id id] ref for :constituent/nutrient
       (filter (comp #{nutrient-id} second :constituent/nutrient))
       first
       :measurement/quantity))

(defn calculate-vitamin-a-quantity-2024
  "Calculates Vit A per 2024 as VITA[µg] = RETOL[µg] + (CARTB[µg] / 6)"
  [constituents]
  (let [retol (get-nutrient-quantity "Retinol" constituents)
        cartb (get-nutrient-quantity "B-karo"  constituents)]
    (when (and (some-> retol b/num)
               (some-> cartb b/num))
      (b/quantity #broch/quantity [0 "µg-RE"] (b/num (b/+ retol (b// cartb 6)))))))

(defn get-vitamin-a-2024 [constituents]
  {:constituent/nutrient [:nutrient/id "Vit A RE"]
   :measurement/source [:source/id "MI0322"]
   :measurement/quantity (or (calculate-vitamin-a-quantity-2024 constituents)
                             #broch/quantity[0 "µg"])})

(defn get-constituents [food id->nutrient]
  (set
   (for [[id {:strs [ref value]}] (->> (apply dissoc food known-non-constituents)
                                       (filter (fn [[_ v]] (get v "ref")))
                                       (filter (comp id->nutrient first)))]
     (let [amount (parse-double value)]
       (cond-> {:constituent/nutrient [:nutrient/id id]
                :measurement/source [:source/id ref]}
         amount (assoc :measurement/quantity
                       (b/from-edn [amount (get-in id->nutrient [id :nutrient/unit])])))))))

(defn get-portions [{:strs [ref value]} id->portion-kind]
  (->> (mapv (fn [portion-kind-id v]
               (when-not (id->portion-kind portion-kind-id)
                 (throw (ex-info "Unknown portion kind"
                                 {:kind portion-kind-id, :value v})))
               (try
                 (when-let [grams (parse-doublish v)]
                   {:portion/kind [:portion-kind/id portion-kind-id]
                    :portion/quantity (b/grams grams)})
                 (catch Exception e
                   (throw (ex-info "Can't get me no portions" {:ref portion-kind-id :value v} e)))))
             (parse-portion-kinds ref)
             (get-words value))
       (remove nil?)
       set))

(defn get-energy [{:strs [ref value]}]
  (try
    (when-let [kj (parse-doublish value)]
      {:measurement/quantity (misc/kilojoules kj)
       :measurement/source [:source/id ref]})
    (catch Exception e
      (throw (ex-info "Can't get me no energy" {:ref ref :value value} e)))))

(defn get-edible-part [{:strs [ref value]}]
  (try
    (when-let [pct (parse-doublish value)]
      {:measurement/percent (Math/round pct)
       :measurement/source [:source/id ref]})
    (catch Exception e
      (throw (ex-info "Can't get me no edible part" {:ref ref :value value} e)))))

(defn foodcase-food->food [{:strs [id name groupId synonym latinName Netto
                                   langualCodes Energi1 Energi2 Portion] :as food}
                           {:keys [id->nutrient id->portion-kind]}]
  (try
    (->> {:food/id (if (and (#{"Brød, halvgrovt (25-50 %), kjøpt, Odelsbrød"
                               "Bread, semi-coarse (25-50 %), Odelsbrød"} name)
                            (= "05.362" id))
                     "05.359"
                     id)
          :food/name name
          :food/food-group [:food-group/id groupId]
          :food/search-keywords (set (get-words synonym #";"))
          :food/latin-name latinName
          :food/langual-codes (set (for [id (get-words langualCodes)]
                                     [:langual-code/id id]))
          :food/energy (get-energy Energi1)
          :food/calories {:measurement/observation (get Energi2 "value")
                          :measurement/source [:source/id (get Energi2 "ref")]}
          :food/constituents (let [constituents (get-constituents food id->nutrient)]
                               (conj constituents (get-vitamin-a-2024 constituents)))
          :food/portions (get-portions Portion id->portion-kind)
          :food/edible-part (get-edible-part Netto)}
         (remove (comp nil? second))
         (into {}))
    (catch Exception e
      (throw (ex-info "Failed to import food" {:food/id id} e)))))

(defn strip-i18n-attrs [attrs data]
  (walk/postwalk
   (fn [x]
     (if (map? x)
       (apply dissoc x attrs)
       x))
   data))

(defn validate-i18n-combination [locale->ms i18n-attrs]
  (or (let [ms (->> (vals locale->ms)
                    (map #(strip-i18n-attrs i18n-attrs %)))]
        (when-not (apply = ms)
          {:explanation "Localized food sources are not all alike"
           :data {:ms ms}}))
      (when (->> (tree-seq coll? identity locale->ms)
                 (filter set?)
                 (tree-seq coll? identity)
                 (filter i18n-attrs)
                 seq)
        {:explanation "Can't use i18n attributes inside a set"
         :data {}})))

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
    (throw (ex-info (:explanation error) (:data error))))
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

(def outdated-food-group-ids
  #{"1.4.1" "1.4.2" "1.4.3" "2.1" "2.2" "3.1.1" "3.1.2" "3.2.1" "3.2.2"
    "3.3.1" "3.3.2" "3.4.1" "3.4.2" "3.5.1" "3.5.2" "3.6" "3.6.1" "3.6.2"
    "4.1.1.1" "4.1.1.2" "4.1.2.1" "4.1.2.2" "4.1.4" "5.2.1" "5.2.2"
    "5.11" "6.1" "6.2.1" "6.2.2" "6.2.3" "6.3" "6.4" "6.4.1" "6.4.2"
    "6.5" "8.4" "10.1" "10.2" "10.3" "10.4" "10.8"})

(defn foodcase-foodgroup->food-group [{:strs [id parentId name]}]
  (when (and (seq id)
             (not (outdated-food-group-ids id)))
    (cond-> {:food-group/id id
             :food-group/name name}
      (seq parentId)
      (assoc :food-group/parent {:food-group/id parentId}))))

(defn foodcase-nutrient->nutrient [{:strs [id name euroFIR euroFIRname unit decimals parentId]}]
  (when (and (not (known-non-constituents id)) (not-empty unit))
    (let [parent (nutrient/get-parent id parentId)]
      (cond-> {:nutrient/id id
               :nutrient/name name
               :nutrient/euro-fir-id euroFIR
               :nutrient/euro-fir-name euroFIRname
               :nutrient/unit unit
               :nutrient/decimal-precision (or (nutrient-id->decimal-precision id)
                                               (some-> decimals parse-long))}
        parent
        (assoc :nutrient/parent parent)))))

(defn foodcase-reference->source [{:strs [id text]}]
  {:source/id id
   :source/description text})

(def apriori-sources
  [{:source/id "MI0322"
    :source/description
    {:en "Vitamin A activity calculated from retinol and beta-carotene (factor 1/6) (VITA[µg] = RETOL[µg] + (CARTB[µg] / 6))"
     :nb "Vitamin A-aktivitet beregnet fra retinol og betakaroten (faktor 1/6) (VITA[µg] = RETOL[µg] + (CARTB[µg] / 6))"}}])

(defn foodcase-langualcode->langual-code [{:strs [id text]}]
  {:langual-code/id id
   :langual-code/description text})

(def english-portion-kinds
  {"desiliter" "decilitre"
   "glass" "glass"
   "glass (lite)" "glass (small)"
   "glass (stort)" "glass (large)"
   "kopp" "cup"
   "beger" "cup"
   "spiseskje" "tablespoon"
   "teskje" "teaspoon"
   "kartong" "carton"
   "porsjon" "portion"
   "stk" "pcs"
   "stk (liten)" "pcs (small)"
   "stk (middels)" "pcs (medium)"
   "stk (stor)" "pcs (large)"
   "filet" "fillet"
   "stang" "bar"
   "skive" "slice"
   "ferdig skivet" "pre-sliced"
   "pr brødskive" "for a slice of bread"
   "plate" "bar"
   "plate (liten)" "bar (small)"
   "plate (middels)" "bar (medium)"
   "plate (stor)" "bar (large)"
   "pakke" "package"
   "boks" "box"
   "boks (liten)" "box (small)"
   "boks (stor)" "box (large)"
   "neve" "handful"
   "pose" "package"
   "pose (liten)" "package (small)"
   "pose (stor)" "package (large)"
   "bukett" "floret"
   "blad" "leaf"
   "stilk" "stalk"
   "cm rot" "cm of root"
   "båt" "section"
   "ring" "ring"
   "fedd" "clove"
   "terning" "cube"})

(defn foodcase-portiontype->portion-kind [{:strs [id name unit]}]
  (let [name (str/lower-case name)]
    {:portion-kind/id (-> id
                          (str/replace #" +" "_")
                          (str/replace #"[\(\)]" "")
                          keyword)
     :portion-kind/name {:nb name
                         :en (or (english-portion-kinds name)
                                 (throw (ex-info (str  "Missing translation for portion kind " name) {:id id :name name :unit unit})))}
     :portion-kind/unit unit}))

(defn validate-foods [foods]
  (when-let [duplicate-ids (->> (map :food/id foods)
                                frequencies
                                (remove #(= 1 (second %)))
                                (map first)
                                seq)]
    (throw (ex-info "Found duplicate :food/ids" {:ids (set duplicate-ids)})))
  foods)

(defn load-nutrients [i18n-attrs locale->datas]
  (into
   (combine-i18n-sources
    (update-vals locale->datas #(keep foodcase-nutrient->nutrient (get % "nutrients")))
    i18n-attrs)
   nutrient/apriori-nutrients))

(defn create-foodcase-transactions [db locale->datas]
  (let [i18n-attrs (db/get-i18n-attrs db)
        nutrients (load-nutrients i18n-attrs locale->datas)
        portion-kinds (map foodcase-portiontype->portion-kind (get (first (vals locale->datas)) "portiontypes"))]
    [;; food-groups
     (combine-i18n-sources
      (update-vals locale->datas #(keep foodcase-foodgroup->food-group (get % "foodgroups")))
      i18n-attrs)

     ;; nutrients
     nutrients

     ;; faux nutrient groups
     (nutrient/get-apriori-groups)

     ;; sources (FoodCASE calls them references, but trust me - they're sources)
     (combine-i18n-sources
      (update-vals locale->datas #(map foodcase-reference->source (get % "references")))
      i18n-attrs)

     ;; A priori sources
     apriori-sources

     ;; langual-codes
     (map foodcase-langualcode->langual-code (get (first (vals locale->datas)) "langualcodes"))

     ;; portion-kinds
     portion-kinds

     ;; foods
     (let [opt {:id->nutrient (into {} (map (juxt :nutrient/id identity) nutrients))
                :id->portion-kind (into {} (map (juxt :portion-kind/id identity) portion-kinds))}]
       (validate-foods
        (combine-i18n-sources
         (update-vals
          locale->datas
          #(map (fn [food] (foodcase-food->food food opt)) (get % "foods")))
         i18n-attrs)))]))

(defn get-content-hash []
  (hash (for [file ["data/foodcase-data-en.json"
                    "data/foodcase-data-nb.json"
                    "data/foodcase-food-en.json"
                    "data/foodcase-food-nb.json"
                    "data/nutrient-decimal-precision.edn"]]
          (slurp (io/file file)))))

(defn create-database [uri]
  (let [schema (read-string (slurp (io/resource "foods-schema.edn")))]
    (db/create-database uri schema)))

(defn load-locale->datas []
  {:nb (merge (load-json "data/foodcase-data-nb.json")
              (load-json "data/foodcase-food-nb.json"))
   :en (merge (load-json "data/foodcase-data-en.json")
              (load-json "data/foodcase-food-en.json"))})

(defn create-database-from-scratch [uri]
  (d/delete-database uri)
  (let [conn (create-database uri)]
    (doseq [tx (create-foodcase-transactions (d/db conn) (load-locale->datas))]
      @(d/transact conn tx))
    conn))

(defn create-data-txes [db]
  (create-foodcase-transactions
   db
   {:nb (merge (load-json "data/foodcase-data-nb.json"))
    :en (merge (load-json "data/foodcase-data-en.json"))}))

(defn create-data-database [uri]
  (let [conn (create-database uri)]
    (doseq [tx (create-data-txes (d/db conn))]
      @(d/transact conn tx))
    conn))

(defn find-new-food-ids [new-foods old-foods]
  (set/difference
   (set (map #(get % "id") (get (load-json new-foods) "foods")))
   (set (map #(get % "id") (get (load-json old-foods) "foods")))))

(defn summarize-food [{:strs [id name groupId synonym latinName
                              langualCodes Energi1 Energi2 Portion]}]
  {:id id
   :name name
   :synonym synonym
   :latinName latinName
   :groupId groupId
   :langualCodes langualCodes
   :Energi1 Energi1
   :Energi2 Energi2
   :Portion Portion})

(comment
  (def conn (create-database-from-scratch "datomic:mem://matvaretabellen"))
  (def conn matvaretabellen.dev/conn)
  (def db (d/db conn))

  (def locale->datas (load-locale->datas))

  (load-nutrients (db/get-i18n-attrs db) locale->datas)

  (get (first (vals locale->datas)) "portiontypes")

  (def raw-foods (->> (get (load-json "data/foodcase-food-nb.json") "foods")
                      (map (juxt #(get % "id") identity))
                      (into {})))

  (summarize-food (raw-foods "01.228"))

  (->> (get (load-json "data/foodcase-food-en.json") "foods")
       (filter (comp #{"05.362"} #(get % "id")))
       (map summarize-food))

  (find-new-food-ids "data/foodcase-food-nb.json" "data/foodcase-food-nb.old.json")

  )
