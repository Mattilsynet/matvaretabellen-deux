(ns matvaretabellen.food
  (:require [broch.core :as b]
            [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.misc :as misc]
            [matvaretabellen.nutrient :as nutrient]))

(defn get-nutrient-measurement [food nutrient-id]
  (->> (:food/constituents food)
       (filter (comp #{nutrient-id} :nutrient/id :constituent/nutrient))
       first))

(defn format-number [n {:keys [decimals]}]
  (let [decimals (if (= (Math/floor n) n)
                   0
                   (or decimals 1))]
    [:i18n :i18n/number {:n n :decimals decimals}]))

(defn wrap-in-portion-span [num & [{:keys [decimals class]}]]
  [:span (cond-> {:data-portion (str num)
                  :data-value (str num)}
           decimals (assoc :data-decimals (str decimals))
           class (assoc :class class))
   (format-number num {:decimals decimals})])

(defn get-calculable-quantity [measurement & [opt]]
  (when-let [q (:measurement/quantity measurement)]
    (list (wrap-in-portion-span (b/num q) opt) " "
          [:span.mvt-sym (b/symbol q)])))

(defn get-nutrient-quantity [food nutrient-id]
  (let [measurement (get-nutrient-measurement food nutrient-id)]
    (or (some-> measurement
                (get-calculable-quantity {:decimals (-> measurement :constituent/nutrient :nutrient/decimal-precision)}))
        "–")))

(defn get-nutrients [food nutrient-id]
  (->> (:food/constituents food)
       (map :constituent/nutrient)
       (filter (comp #{nutrient-id}
                     :nutrient/id
                     :nutrient/parent))
       nutrient/sort-by-preference
       seq))

(defn get-nutrient-group [food nutrient-id]
  {:food food
   :nutrients (get-nutrients food nutrient-id)
   :group (d/entity (d/entity-db food) [:nutrient/id nutrient-id])})

(defn get-flattened-nutrient-group [food nutrient-id]
  (-> (get-nutrient-group food nutrient-id)
      (update :nutrients #(mapcat
                           (fn [nutrient]
                             (or (get-nutrients food (:nutrient/id nutrient))
                                 [nutrient]))
                           %))))

(defn humanize-langual-classification [text]
  (->> (re-seq #"([^\.]+)(?:([\.] ))?" text)
       (map
        (fn [[_ part separator]]
          (str (->> (str/trim part)
                    (re-seq #"([^\(]+)(?:(\([^\)]+\)))?")
                    (map (fn [[_ text parens]]
                           (str/join [(str/capitalize text) parens])))
                    str/join)
               (some-> separator str/trim))))
       (str/join " ")))

(defn get-langual-codes [food]
  (->> (:food/langual-codes food)
       (sort-by :langual-code/id)))

(defn get-sources [food]
  (->> (:food/constituents food)
       (keep :measurement/source)
       set
       (sort-by (comp misc/natural-order-comparator-ish :source/id))))

(defn hyperlink-string [desc]
  (loop [words (-> (str/replace desc #"," ", ")
                   (str/split #"\s+"))
         res []]
    (let [word (first words)]
      (cond
        (nil? word)
        (let [groups (partition-by type res)]
          (mapcat
           (fn [i xs]
             (if (string? (first xs))
               [(str (when (not= 0 i) " ")
                     (str/join " " xs)
                     (when (not= (dec (count groups)) i) " "))]
               (interpose " " xs)))
           (range)
           groups))

        (re-find #"https?://.+" word)
        (recur
         (next words)
         (conj res (let [url (str/trim (str/replace word #" " ""))]
                     [:a {:href url}
                      (if (< 35 (count url))
                        (second (re-find #"https?://([^/]+)/" url))
                        url)])))

        :else
        (recur (next words) (conj res word))))))

(defn ->nutrient-lookup [constituents]
  (->> constituents
       (map (juxt (comp :nutrient/id :constituent/nutrient) (comp b/num :measurement/quantity)))
       (into {})))

(defn get-nutrient-group-lookup [food]
  (->> (:food/constituents food)
       (remove (comp :nutrient/parent :constituent/nutrient))
       ->nutrient-lookup))

(defn food->diffable [food]
  [(:food/id food) (get-nutrient-group-lookup food)])

(defn get-first-word [s]
  (first (str/split s #",")))

(defn infer-food-kind [food locale]
  (get-first-word (get-in food [:food/name locale])))

(defn get-variant-name [food locale inferred-kind]
  (-> (get-in food [:food/name locale])
      (str/replace (re-pattern (str "^" inferred-kind)) "")
      (str/replace #"^\s*," "")
      str/trim))

(defn find-related-foods [food locale]
  (let [db (d/entity-db food)
        categoryish (infer-food-kind food locale)]
    (->> (d/q '[:find [?f ...]
                :where
                [?f :food/id]]
              db)
         (map #(d/entity db %))
         (filter #(= categoryish (infer-food-kind % locale)))
         (remove #(= (:db/id food) (:db/id %)))
         seq)))

(comment

  (def conn matvaretabellen.dev/conn)

  (def dried-apple "06.531")
  (def banana "06.525")

  (->> "WaterSolubleVitamins"
       (get-nutrients (d/entity (d/db conn) [:food/id banana]))
       (map #(into {} %)))

  (->> (d/entity (d/db conn) [:nutrient/id "Vit A"])
       :nutrient/_parent
       (map #(into {} %)))

  (d/q '[:find ?d
         :where
         [_ :langual-code/description ?d]]
       (d/db conn))
)
