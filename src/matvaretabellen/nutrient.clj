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

(defn get-used-nutrients [food-db]
  (for [eid (d/q '[:find [?n ...]
                   :where
                   [?n :nutrient/id]
                   [?c :constituent/nutrient ?n]]
                 food-db)]
    (d/entity food-db eid)))

(def sort-names
  (->> ["Fett"
        "Karbo"
        "Fiber"
        "Stivel"
        "Mono+Di"
        "Sukker"
        "SUGAN"
        "Protein"
        "Vann"
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
        "Se"
        "FatSolubleVitamins"
        "WaterSolubleVitamins"
        "Minerals"
        "TraceElements"]
       (map-indexed #(vector %2 (format " %02d" %1)))
       (into
        {"Niacin" "Vit B03"
         "NIAEQ" "Vit B031"
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
                         "NIAEQ"
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

(def apriori-parent-id
  {"SUGAN" "Karbo"})

(defn get-parent
  "The FoodCase data currently does not group certain nutrients that we want
  grouped, such as vitamins. This function provides a apriori parent while we
  wait for more structured source data."
  [id parent-id]
  (or (when (seq parent-id)
        {:nutrient/id parent-id})
      (when-let [parent-id (get apriori-index id)]
        {:nutrient/id parent-id})
      (when-let [parent-id (apriori-parent-id id)]
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

(defn get-top-level-nutrients
  "Get all top level nutrients that contains at least one nutrient for which we
  have measurements."
  [foods-db]
  (->> (d/q '[:find [?n ...]
              :where
              [?n :nutrient/id]
              (not [?n :nutrient/parent])
              (or-join [?n]
                       [_ :constituent/nutrient ?n]
                       (and [?sn :nutrient/parent ?n]
                            [_ :constituent/nutrient ?sn]))]
            foods-db)
       (map #(d/entity foods-db %))))

(def default-checked #{"Fett" "Karbo" "Protein" "Fiber"})

(defn ->filter-option [selectable? nutrient]
  (let [children (sort-by-preference (:nutrient/_parent nutrient))]
    (cond-> {:label [:i18n :i18n/lookup (:nutrient/name nutrient)]
             :sort-id (:nutrient/id nutrient)}
      (selectable? nutrient)
      (merge {:data-filter-id (:nutrient/id nutrient)
              :checked? (boolean (default-checked (:nutrient/id nutrient)))})

      (seq children)
      (assoc :options (map #(->filter-option selectable? %) children)))))

(defn create-filters [foods-db]
  (let [selectable? (comp (->> (get-used-nutrients foods-db)
                               (map :nutrient/id)
                               set)
                          :nutrient/id)
        nutrients (get-top-level-nutrients foods-db)
        has-children? (comp boolean seq :nutrient/_parent)
        by-children (group-by has-children? nutrients)]
    (conj (map #(assoc (->filter-option selectable? %) :class :mmm-h6) (get by-children true))
          (let [nutrients (get by-children false)]
            {:sort-id (:nutrient/id (first nutrients))
             :options (->> (sort-by-preference nutrients)
                           (map #(->filter-option selectable? %)))}))))

(defn count-filter-options [filter-m]
  (->> (tree-seq coll? identity filter-m)
       (filter #{:label})
       count))

(defn balance-filters [filters columns]
  (loop [filters (->> filters
                      (map (juxt count-filter-options identity))
                      (sort-by (comp - first)))
         lengths (vec (repeat columns 0))
         columns (vec (repeat columns []))]
    (if-let [[n filter-m] (first filters)]
      (let [idx (.indexOf lengths (apply min lengths))]
        (recur
         (next filters)
         (update lengths idx + n)
         (update columns idx conj filter-m)))
      (for [column columns]
        (sort-by
         (fn [filter-m]
           (let [id (or (:data-filter-id filter-m)
                        (:sort-id filter-m))]
             (sort-names id id)))
         column)))))

(defn prepare-filters [foods-db & [{:keys [columns]}]]
  (balance-filters (create-filters foods-db) (or columns 2)))

(comment

  (def conn matvaretabellen.dev/conn)
  (get-nutrient-statistics (d/db conn) matvaretabellen.statistics/get-median)

)
