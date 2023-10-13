(ns matvaretabellen.ui.foods-search
  (:require [clojure.set :as set]
            [matvaretabellen.search :as search]))

(defn score-term [index fields term]
  (->> fields
       (mapcat (fn [field]
                 (for [[id weight] (get-in index [field term])]
                   {:id id
                    :score weight
                    :field field})))
       (group-by :id)
       (map (fn [[id xs]]
              {:id id
               :score (reduce + 0 (map :score xs))
               :fields (into {} (map (juxt :field :score) xs))
               :term term}))))

(defn qualified-match? [terms res {:keys [operator min-accuracy]}]
  (<= (cond
        (and (= :or operator) min-accuracy)
        (* min-accuracy (count terms))

        (= :or operator)
        1

        :else (count terms))
      (count res)))

(defn match-query [index {:keys [q boost tokenizers token-filters fields] :as query}]
  (let [fields (or fields (keys index))
        boost (or boost 1)
        terms (->> (search/tokenize q tokenizers)
                   (search/filter-tokens token-filters))]
    (->> terms
         (mapcat #(score-term index fields %))
         (group-by :id)
         (filter (fn [[_ xs]]
                   (qualified-match? terms xs query)))
         (map (fn [[id xs]]
                {:id id
                 :score (* boost (reduce + 0 (map :score xs)))
                 :fields (->> (for [[k score] (apply merge-with + (map :fields xs))]
                                [k (* boost score)])
                              (into {}))
                 :terms (->> xs
                             (map (juxt :term (comp #(* boost %) :score)))
                             (into {}))})))))

(defn query
  "Query the index created by `index-document` with `q`. `q` is a map with two
  keys:

  - `:queries` A seq of maps defining a query (see below)
  - `:operator` Either `:or` or `:and` (default)

  Each query in `:queries` is a map of the following keys:

  - `:q` The query string
  - `:tokenizers` How to tokenize the query string before matching against
                  indexes. Defaults to `default-tokenizers`.
  - `:token-filters` Filter tokens
  - `:fields` What field indexes to match against. Defaults to all fields.
  - `:boost` A score boost for this query.
  - `:operator` Either `:or` or `:and` (default). Determines whether a
                single token match is good enough (`:or`), or if all tokens must
                match (`:and`).
  - `:min-accuracy` When `:operator` is `:or`, this can be a number between `0`
                    and `1` determining the lowest acceptable success rate. `0.5`
                    means that at least half the tokens from `q` must match tokens
                    in the queried indexes

  Each query will possibly find some results. Results scored based on the number
  of matching tokens. These scores are then boosted for each individual query.
  The final result will be either the intersection of all sub-results (`:and`),
  or the union (`:or`). The final score for each document id will be calculated
  by summarizing individual query scores, and `query` returns a sorted seq of
  results, with the best scoring result first.

  Results are maps of:

  - `:id` The id of the document
  - `:score` The calculated total score
  - `:fields` A map of `{field score}` - e.g. what fields contributed to the
              result, and their individual scores.
  - `:terms` A map of `{term score}` - e.g. what terms contributed to the result,
             and their individual scores."
  [index q]
  (let [res (map #(match-query index %) (:queries q))
        ids (map #(set (map :id %)) res)
        res-ids (if (= :or (:operator q))
                  (apply set/union ids)
                  (apply set/intersection ids))]
    (->> (apply concat res)
         (filter (comp res-ids :id))
         (group-by :id)
         (map (fn [[id xs]]
                {:id id
                 :score (reduce + 0 (map :score xs))
                 :fields (apply merge-with + (map :fields xs))
                 :terms (apply merge-with + (map :terms xs))}))
         (sort-by (comp - :score)))))

(defn search [engine q]
  (for [match
        (query
         (:index engine)
         {:queries [;; "Autocomplete" what the user is typing
                    (-> (:foodNameEdgegrams (:schema engine))
                        (assoc :q q)
                        (assoc :fields ["foodNameEdgegrams"])
                        (assoc :boost 10))
                    ;; Boost exact matches
                    (-> (:foodName (:schema engine))
                        (assoc :q q)
                        (assoc :fields ["foodName"])
                        (assoc :boost 5))
                    ;; Add fuzziness
                    (-> (:foodNameNgrams (:schema engine))
                        (merge {:q q
                                :fields ["foodNameNgrams"]
                                :operator :or
                                :min-accuracy 0.8
                                }))]
          :operator :or})]
    (assoc match :name (get (:foods engine) (:id match)))))
