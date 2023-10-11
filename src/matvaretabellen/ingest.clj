(ns matvaretabellen.ingest
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.pages :as pages]))

(defn on-started [_foods-conn powerpack-app]
  @(d/transact (:datomic/conn powerpack-app) pages/static-pages))

(defn create-tx [_file-name datas]
  datas)
