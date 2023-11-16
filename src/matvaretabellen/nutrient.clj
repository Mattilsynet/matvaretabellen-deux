(ns matvaretabellen.nutrient
  (:require [broch.core :as b]
            [datomic-type-extensions.api :as d]))

(defn get-foods-by-nutrient-density [nutrient]
  (when-let [db (some-> nutrient d/entity-db)]
    (->> (d/q '[:find ?f ?q
                :in $ ?n
                :where
                [?c :constituent/nutrient ?n]
                [?c :measurement/quantity ?q]
                [?f :food/constituents ?c]]
              db
              (:db/id nutrient))
         (filter #(< 0 (b/num (second %))))
         (sort-by second)
         reverse
         (map #(d/entity db (first %))))))

(def sort-names
  (->> ["Fett"
        "Karbo"
        "Stivel"
        "Mono+Di"
        "Sukker"
        "Protein"
        "Mettet"
        "Trans"
        "Enumet"
        "Flerum"
        "Omega-3"
        "Omega-6"
        "Kolest"
        "Ca"
        "K"
        "Na"
        "NaCl"
        "P"
        "Fe"
        "Cu"
        "Zn"
        "Se"]
       (map-indexed #(vector %2 (format " %02d" %1)))
       (into
        {"Niacin" "Vit B03"
         "Folat" "Vit B09"
         "Vit B1" "Vit B01"
         "Vit B2" "Vit B02"
         "Vit B6" "Vit B06"
         "Retinol" "A Retinol"
         "B-karo" "A Betakaroten"})))

(defn sort-by-preference [nutrients]
  (->> nutrients
       (sort-by (comp #(sort-names % %) :nutrient/id))))

(def apriori-groups
  (->> [{:nutrient/id "WaterSolubleVitamins"
         :nutrient/name {:nb "Vannløselige vitaminer"
                         :en "Water-soluble vitamins"}
         ::nutrient-ids ["Vit B1"
                         "Vit B12"
                         "Vit B2"
                         "Folat"
                         "Niacin"
                         "Vit B6"
                         "Vit C"]}
        {:nutrient/id "FatSolubleVitamins"
         :nutrient/name {:nb "Fettløselige vitaminer"
                         :en "Fat-soluble vitamins"}
         ::nutrient-ids ["Vit A" "Vit D" "Vit E"]}
        {:nutrient/id "Minerals"
         :nutrient/name {:nb "Mineraler"
                         :en "Minerals"}
         ::nutrient-ids ["Ca" "K" "Mg" "Na" "NaCl" "P"]}
        {:nutrient/id "TraceElements"
         :nutrient/name {:nb "Sporstoffer"
                         :en "Trace elements"}
         ::nutrient-ids ["Fe" "I" "Cu" "Se" "Zn"]}]
       (map (juxt :nutrient/id identity))
       (into {})))

(def apriori-index
  (->> (vals apriori-groups)
       (mapcat #(map (fn [id] [id (:nutrient/id %)])
                     (::nutrient-ids %)))
       (into {})))

(defn get-parent
  "The FoodCase data currently does not group certain good groups that we want
  grouped, such as vitamins. This function provides a apriori parent while we
  wait for more structured source data."
  [id parent-id]
  (or (when (seq parent-id)
        {:nutrient/id parent-id})
      (when-let [parent-id (get apriori-index id)]
        {:nutrient/id parent-id})))

(defn get-apriori-groups []
  (map #(dissoc % ::nutrient-ids) (vals apriori-groups)))

(defn get-nutrient-statistics [db f]
  (->> (d/q '[:find ?id ?q
              :where
              [?c :measurement/quantity ?q]
              [?c :constituent/nutrient ?n]
              [?n :nutrient/id ?id]]
            db)
       (group-by first)
       (map (fn [[k xs]]
              [k (f (map (comp b/num second) xs))]))
       (sort-by first)
       (into {})))

(comment

  (def conn matvaretabellen.dev/conn)
  (get-nutrient-statistics (d/db conn) matvaretabellen.statistics/get-median)

)
