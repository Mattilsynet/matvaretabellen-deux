(ns matvaretabellen.faux-food-db
  (:require [clojure.java.io :as io]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.db :as db]
            [matvaretabellen.foodcase-import :as foodcase-import]))

(defn create-test-db [tx]
  (let [uri (str "datomic:mem://" (random-uuid))
        schema (read-string (slurp (io/resource "foods-schema.edn")))
        conn (db/create-database uri schema)]
    @(d/transact conn tx)
    {:conn conn
     :db (d/db conn)}))

(defmacro with-test-db
  {:clj-kondo/lint-as 'clojure.core/let}
  [[db-sym tx] & body]
  `(let [m# (create-test-db ~tx)
         ~db-sym (:db m#)
         res# (do ~@body)]
     (d/release (:conn m#))
     res#))

(def test-conn (atom nil))

(defn get-food-data-db [& [txes]]
  (when-not @test-conn
    (reset! test-conn (foodcase-import/create-data-database "datomic:mem://rda-test")))
  (if txes
    (:db-after (d/with (d/db @test-conn) txes))
    (d/db @test-conn)))
