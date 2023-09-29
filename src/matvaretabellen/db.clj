(ns matvaretabellen.db
  (:require [datomic-type-extensions.api :as d]
            [datomic-type-extensions.types :refer [define-dte]]))

(define-dte :i18n/edn :db.type/string
  [this] (pr-str this)
  [^String s] (read-string s))

(define-dte :broch/quantity :db.type/string
  [this] (pr-str this)
  [^String s] (read-string s))

(defn create-database [uri schema]
  (d/create-database uri)
  (let [conn (d/connect uri)]
    @(d/transact conn schema)
    conn))

(defn get-i18n-attrs [db]
  (set (d/q '[:find [?a ...]
              :where
              [?e :dte/valueType :i18n/string]
              [?e :db/ident ?a]]
            db)))
