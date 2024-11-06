(ns matvaretabellen.rda
  "Recommended Daily Allowance (RDA - ADI in Norwegian). Functions to import
  ADI/RDA from CSV, and work with the resulting data structures."
  (:require [broch.core :as b]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.misc :as misc]))

(defn blank-line? [s]
  (re-find #"^;+\r?$" s))

(defn parse-nor-double [s]
  (parse-double (str/replace s #"," ".")))

(def kind->key
  {"min" :rda.recommendation/min-amount
   "max" :rda.recommendation/max-amount
   "gjsn" :rda.recommendation/average-amount})

(def nb-aliases
  {"Generell 10 mj" "Generell 18-70 år"})

(def en-dictionary
  {"Aktiv 2-3 timer trening per uke" "Active 2-3 hours of exercise per week"
   "Ammende" "Breastfeeding"
   "Fysisk hardt arbeid" "Physically demanding work"
   "Generell 18-70 år" "General 18-70 years"
   "Gjennomsnittlig aktivitetsnivå" "Moderate activity level"
   "Gravid" "Pregnant"
   "Gutt 1-3 år" "Boy 1-3 years"
   "Gutt 11-14 år" "Boy 11-14 years"
   "Gutt 15-17 år" "Boy 15-17 years"
   "Gutt 4-6 år" "Boy 4-6 years"
   "Gutt 7-10 år" "Boy 7-10 years"
   "Høyt aktivitetsnivå" "High activity level"
   "Institusjon - Energi- og næringstett kost" "Institution - Energy and Nutrient-dense Diet."
   "Institusjon - Nøkkelrådskost" "Institution - Key Advisory Cost."
   "Jente 1-3 år" "Girl 1-3 years"
   "Jente 11-14 år" "Girl 11-14 years"
   "Jente 15-17 år" "Girl 15-17 years"
   "Jente 4-6 år" "Girl 4-6 years"
   "Jente 7-10 år" "Girl 7-10 years"
   "Kvinne 18-24 år" "Woman 18-24 years"
   "Kvinne 25-50 år" "Woman 25-50 years"
   "Kvinne 51-70 år" "Woman 51-70 years"
   "Kvinne 70+ år" "Woman 70+ years"
   "Lavt aktivitetsnivå" "Low activity level"
   "Lite aktiv mindre enn 2 timer trening per uke" "Sedentary, less than 2 hours of exercise per week"
   "Mann 18-24 år" "Man 18-24 years"
   "Mann 25-50 år" "Man 25-50 years"
   "Mann 51-70 år" "Man 51-70 years"
   "Mann 70+ år" "Man 70+ years"
   "Sengeliggende/inaktiv" "Bedridden/Inactive"
   "Spedbarn 12-23 mnd" "Infant 12-23 months"
   "Spedbarn 7-11 mnd" "Infant 7-11 months"
   "Stillesittende arbeid" "Sedentary work"
   "Stående arbeid" "Standing work"
   "Svært aktiv mer enn 3 timer trening per uke" "Very active, more than 3 hours of exercise per week"})

(defn get-recommendation [nutrient header kind v]
  (let [kind (if (re-find #"^<" v) "max" kind)]
    (if (re-find #"(E %)" header)
      [(cond
         (= "max" kind)
         :rda.recommendation/max-energy-pct

         (= "min" kind)
         :rda.recommendation/min-energy-pct

         :else
         :rda.recommendation/average-energy-pct)
       (parse-long (str/replace v #"[^\d]" ""))]
      (let [n (parse-nor-double (str/replace v #"[^\d,]" ""))]
        (cond
          (re-find #"\(g\)" header)
          [(kind->key kind) (b/from-edn [n "g"])]

          :else
          [(kind->key kind)
           (b/from-edn [n (:nutrient/unit nutrient)])])))))

(defn ->recommendation [foods-db [nutrient-id recommendations]]
  (->> (for [[header _ kind v] (remove (comp empty? last) recommendations)]
         (get-recommendation (d/entity foods-db [:nutrient/id nutrient-id]) header kind v))
       (into {:rda.recommendation/nutrient-id nutrient-id})))

(def texts (atom #{}))

(defn internationalize [nb]
  (swap! texts conj nb)
  (let [nb (get nb-aliases nb nb)]
    {:nb nb
     :en (en-dictionary nb)}))

(defn get-demographic [sex-ish age-ish]
  (internationalize
   (str sex-ish
        (when-not (#{"Gravid" "Ammende"} sex-ish)
          (str
           (if (re-find #"^[\d\-\+]+" age-ish)
             " "
             " - ")
           (str/capitalize age-ish)
           (when (or (re-find #"\d-\d+$" age-ish)
                     (re-find #"\d\++$" age-ish))
             " år"))))))

(defn parse-row [foods-db headers row]
  (try
    (let [cols (map str/trim (str/split row #";"))
          leisure-activity (not-empty (nth cols 8))]
      (cond->
          {:rda/id (str "rda" (hash (nth cols 1)))
           :rda/order (parse-long (nth cols 2))
           :rda/demographic (get-demographic (nth cols 3) (nth cols 4))
           :rda/energy-recommendation (misc/kilojoules (parse-nor-double (nth cols 13)))
           :rda/kcal-recommendation (parse-nor-double (nth cols 14))
           :rda/work-activity-level (internationalize (str/capitalize (nth cols 7)))
           :rda/recommendations (->> (map conj (drop 15 headers) (drop 15 cols))
                                     (group-by second)
                                     (remove (comp empty? first))
                                     (map (partial ->recommendation foods-db))
                                     set)}
        leisure-activity (assoc :rda/leisure-activity-level (->> leisure-activity
                                                                 str/capitalize
                                                                 internationalize))))
    (catch Exception e
      (throw (ex-info "Can't parse RDA row"
                      {:headers headers
                       :row row}
                      e)))))

(defn read-csv
  "This reads the CSV file as exported from the very hand-tailored spreadsheet we
  once got from Jorån. I don't know if updates will follow the same format. The
  source CSV file is stored in the data directory of this repo."
  [foods-db csv-str]
  (let [[names ids modes & rows] (str/split csv-str #"\n")
        headers (mapv vector
                      (mapv str/trim (str/split names #";"))
                      (mapv str/trim (str/split ids #";"))
                      (mapv str/trim (str/split modes #";")))]
    (->> rows
         (partition-by blank-line?)
         (partition-all 2)
         (mapcat first)
         (remove blank-line?) ;; May be one dangling blank line
         (mapv #(parse-row foods-db headers %)))))

(defn sort-order [rda-profile]
  (let [demographic (get-in rda-profile [:rda/demographic :nb])]
    [(cond
       (str/starts-with? demographic "Generell") 0
       (str/starts-with? demographic "Kvinne") 1
       (str/starts-with? demographic "Mann") 2
       (str/starts-with? demographic "Jente 1-3 år") 3
       (str/starts-with? demographic "Jente 4-6 år") 4
       (str/starts-with? demographic "Jente 7-10 år") 5
       (str/starts-with? demographic "Jente") 6
       (str/starts-with? demographic "Gutt 1-3 år") 7
       (str/starts-with? demographic "Gutt 4-6 år") 8
       (str/starts-with? demographic "Gutt 7-10 år") 9
       (str/starts-with? demographic "Gutt") 10
       (str/starts-with? demographic "Spedbarn") 11
       (str/starts-with? demographic "Gravid") 12
       (str/starts-with? demographic "Ammende") 13
       :else 14)
     demographic]))

(defn irrelevant? [profile]
  (#{"Spedbarn 12-23 mnd"} (:nb (:rda/demographic profile))))

(defn get-profiles-per-demographic [db]
  (->> (d/q '[:find [?e ...]
              :where
              [?e :rda/id]]
            db)
       (map #(d/entity db %))
       (group-by :rda/demographic)
       (map #(first (sort-by :rda/id (second %))))
       (remove irrelevant?)
       (sort-by sort-order)))

(defn unbroch [q]
  (when q
    [(b/num q) (b/symbol q)]))

(defn recommendation->json [recommendation]
  (->> {:minEnergyPct (:rda.recommendation/min-energy-pct recommendation)
        :maxEnergyPct (:rda.recommendation/max-energy-pct recommendation)
        :averageEnergyPct (:rda.recommendation/average-energy-pct recommendation)
        :minAmount (unbroch (:rda.recommendation/min-amount recommendation))
        :maxAmount (unbroch (:rda.recommendation/max-amount recommendation))
        :averageAmount (unbroch (:rda.recommendation/average-amount recommendation))}
       (remove (comp nil? second))
       (into {})))

(defn ->json [locale profile]
  (cond-> {:id (:rda/id profile)
           :demographic (get (:rda/demographic profile) locale)
           :energyRecommendation (unbroch (:rda/energy-recommendation profile))
           :kcalRecommendation (:rda/kcal-recommendation profile)
           :recommendations (->> (:rda/recommendations profile)
                                 (map (juxt :rda.recommendation/nutrient-id
                                            recommendation->json))
                                 (remove (comp empty? second))
                                 (into {}))}
    (:rda/work-activity-level profile)
    (assoc :workActivityLevel (get (:rda/work-activity-level profile) locale))

    (:rda/leisure-activity-level profile)
    (assoc :leisureActivityLevel (get (:rda/leisure-activity-level profile) locale))))

(defn render-json [context page]
  {:content-type :json
   :body
   {:profiles
    (for [profile (get-profiles-per-demographic (:app/db context))]
      (->json (:page/locale page) profile))}})

(comment

  (def conn matvaretabellen.dev/conn)
  (def app-db matvaretabellen.dev/app-db)

  (def old (read-csv (d/db conn) (slurp (io/file "data/adi.csv"))))
  (def new (read-csv (d/db conn) (slurp (io/file "data/adi2024.csv"))))

  (count old)
  (count new)

  (->> new
       (sort-by :rda/order)
       (map #(select-keys % [:rda/demographic :rda/work-activity-level])))

  (count @texts)
  (remove (set (keys en-dictionary)) @texts)

  (->> (for [k (filter @texts (keys en-dictionary))]
         [k (get en-dictionary k)])
       (into {}))

)
