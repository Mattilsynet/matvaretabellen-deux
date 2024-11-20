(ns matvaretabellen.search-index
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.nutrient :as nutrient]
            [matvaretabellen.search :as search]))

(defn index-document
  "Index data in `doc` according to `schema` under `id` in `index`. Returns the
  updated index. At its simplest, the schema specifies which keys in `doc` to
  include in the index, and how to tokenize them:

  ```clj
  {:title {:tokenizers [tokenize-words]}
   :description {:tokenizers [tokenize-words]}}
  ```

  This schema will use the provided tokenizers to index `:title` and
  `:description` from `doc`.

  The following schema names what function `:f` to apply to `doc` to extract the
  data to index, and what `:tokenizers` to use. The keys of the schema name the
  resulting field indexes - when querying you can choose to query across all
  fields, or name individual fields to query:

  ```clj
  {:title
   {:f :title
    :tokenizers [tokenize-words]}

   :description
   {:f :description
    :tokenizers [tokenize-words]}}
  ```

  You can use schemas to index the same fields multiple times with different
  tokenizers:

  ```clj
  {:title
   {:f :title
    :tokenizers [tokenize-words]}

   :title.ngrams
   {:f :title
    :tokenizers [tokenize-words
                 (partial tokenize-ngrams 3)]}}
  ```"
  [index schema id doc]
  (->> schema
       (mapcat (fn [[field config]]
                 (let [f (:f config field)]
                   (->> (search/tokenize (f doc) (:tokenizers config))
                        (search/filter-tokens (:token-filters config))
                        (search/get-field-syms field)))))
       (reduce (fn [index {:keys [field sym weight]}]
                 (assoc-in index [field sym id] weight))
               index)))

(defn index-foods [schema db & [index]]
  (->> (d/q '[:find [?food ...]
              :where
              [?food :food/id]]
            db)
       (map #(d/entity db %))
       (reduce (fn [index food]
                 (index-document index schema (:food/id food) food))
               index)))

(defn index-nutrients [schema db & [index]]
  (->> (nutrient/get-used-nutrients db)
       (reduce (fn [index nutrient]
                 (index-document index schema (:nutrient/id nutrient) nutrient))
               index)))

(defn build-index [db locale]
  (let [schema (search/create-schema locale)]
    (->> (index-foods schema db)
         (index-nutrients schema db))))

(comment

  (index-document {} (search/create-schema :nb) "lol" {:food/name {:nb "Vitamin D12"}})

  )
