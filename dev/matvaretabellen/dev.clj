(ns matvaretabellen.dev
  (:require [clojure.data.json :as json]
            [clojure.tools.namespace.repl :as repl]
            [confair.config :as config]
            [confair.config-admin :as ca]
            [courier.http :as http]
            [datomic-type-extensions.api :as d]
            [integrant.core :as ig]
            [integrant.repl.state]
            [matvaretabellen.core :as matvaretabellen]
            [matvaretabellen.export :as export]
            [matvaretabellen.foodcase-import :as foodcase-import]
            [matvaretabellen.search-index :as index]
            [powerpack.dev :as dev :refer [start reset]]
            [snitch.core]))

(defn load-local-config []
  (-> (config/from-file "./config/local-config.edn")
      (config/mask-config)))

(defmethod ig/init-key :powerpack/powerpack [_ _]
  (set! *print-namespace-maps* false)
  (repl/set-refresh-dirs "src" "dev" "test" "ui/src")
  (matvaretabellen/create-dev-app (load-local-config)))

(comment

  (def config (load-local-config))

  ;; If your database is empty ("Could not find foods in catalog")
  ;; Remember to `make start-transactor`
  (time
   (foodcase-import/create-database-from-scratch (:foods/datomic-uri config)))

  (start)
  (reset)

  (export/export)

  (def conn (d/connect (:foods/datomic-uri config)))

  (->> (d/q '[:find [(pull ?nutrient [:nutrient/id :nutrient/name]) ...]
              :in $ ?parent
              :where
              [?p :nutrient/id ?parent]
              [?nutrient :nutrient/parent ?p]]
            (d/db conn)
            "FatSolubleVitamins")
       (take 5))

  (->> (d/entity (d/db conn) [:food/id "06.531"])
       (into {}))

  (index/index-foods {} (d/db conn) :en)

  (seq (d/datoms (d/db (:datomic/conn integrant.repl.state/system))
                 :avet :page/uri))

  ;; config-admin
  (ca/conceal-value (config/from-file "./config/local-config.edn")
                    :secret/dev
                    :foodcase/bearer-token)

  (ca/conceal-value (config/from-file "./config/prod-config.edn")
                    :secret/prod
                    :foodcase/bearer-token))

(comment

  (def res (http/request
            {:req {:method :get
                   :url (str (:foodcase/base-url config) "/aggregated-foods")
                   :query-params {:size "5"}
                   :headers {"Authorization" (str "Bearer " (:foodcase/bearer-token config))}
                   :accept :json}}))

  (def body (json/read-str (:body res) :key-fn keyword))

  (first (:content body))

  )
