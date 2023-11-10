(ns matvaretabellen.rda
  "Recommended Daily Allowance (RDA - ADI in Norwegian). Functions to import
  ADI/RDA from CSV, and work with the resulting data structures."
  (:require [broch.core :as b]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.misc :as misc]))

(defn blank-line? [s]
  (re-find #"^;+\r$" s))

(defn parse-nor-double [s]
  (parse-double (str/replace s #"," ".")))

(def kind->key
  {"min" :rda.recommendation/min-amount
   "max" :rda.recommendation/max-amount
   "gjsn" :rda.recommendation/average-amount})

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
          (re-find #"(g)" header)
          [(kind->key kind) (b/from-edn [n "g"])]

          :else
          [(kind->key "max")
           (b/from-edn [n (:nutrient/unit nutrient)])])))))

(defn ->recommendation [foods-db [nutrient-id recommendations]]
  (->> (for [[header _ kind v] (remove (comp empty? last) recommendations)]
         (get-recommendation (d/entity foods-db [:nutrient/id nutrient-id]) header kind v))
       (into {:rda.recommendation/nutrient-id nutrient-id})))

(defn parse-row [foods-db headers row]
  (let [cols (map str/trim (str/split row #";"))
        age-ish (nth cols 4)
        leasure-activity (not-empty (nth cols 8))]
    (cond->
        {:rda/id (parse-long (nth cols 2))
         :rda/demography (str (nth cols 3)
                              (if (re-find #"^[\d\-\+]+" age-ish)
                                ", "
                                " - ")
                              (str/capitalize age-ish))
         :rda/energy-recommendation (misc/kilojoules (parse-nor-double (nth cols 13)))
         :rda/kcal-recommendation (parse-nor-double (nth cols 14))
         :rda/work-activity-level (str/capitalize (nth cols 7))
         :rda/recommendations (->> (map conj (drop 15 headers) (drop 15 cols))
                                   (group-by second)
                                   (remove (comp empty? first))
                                   (map (partial ->recommendation foods-db)))}
      leasure-activity (assoc :rda/leasure-activity-level (str/capitalize leasure-activity)))))

(defn read-csv
  "This reads the CSV file as exported from the very hand-tailored spreadsheet we
  once got from Jorån. I don't know if updates will follow the same format. The
  source CSV file is stored in the data directory of this repo."
  [foods-db csv-str]
  (let [[names ids modes & rows] (str/split csv-str #"\n")
        headers (map vector
                     (map str/trim (str/split names #";"))
                     (map str/trim (str/split ids #";"))
                     (map str/trim (str/split modes #";")))]
    (->> rows
         (partition-by blank-line?)
         (partition-all 2)
         (mapcat first)
         (map #(parse-row foods-db headers %)))))

(comment

  (def conn matvaretabellen.dev/conn)

  (read-csv (d/db conn) (slurp (io/file "data/adi.csv")))

)
