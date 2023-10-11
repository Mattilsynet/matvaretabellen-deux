(ns matvaretabellen.dev
  (:require [clojure.data.json :as json]
            [confair.config :as config]
            [confair.config-admin :as ca]
            [courier.http :as http]
            [datomic-type-extensions.api :as d]
            [integrant.core :as ig]
            [integrant.repl.state]
            [matvaretabellen.core :as matvaretabellen]
            [matvaretabellen.foodcase-import :as foodcase-import]
            [matvaretabellen.search :as search]
            [powerpack.app :as app]))

(def config (-> (config/from-file "./config/local-config.edn")
                (config/mask-config)))

(defmethod ig/init-key :powerpack/app [_ _]
  (set! *print-namespace-maps* false)
  (matvaretabellen/create-dev-app config))

(comment

  ;; If your database is empty
  ;; Remember to `make start-transactor`
  (time
   (foodcase-import/create-database-from-scratch (:foods/datomic-uri config)))

  (app/start)
  (app/stop)
  (app/reset)

  (def conn (d/connect (:foods/datomic-uri config)))

  (take 5 (d/q '[:find [(pull ?food [:food/name :food/id]) ...]
                 :where
                 [?food :food/id ?id]]
               (d/db conn)))


  (search/index-foods {} (d/db conn) :en)

  (seq (d/datoms (d/db (:datomic/conn integrant.repl.state/system))
             :avet :page/uri))

  ;; config-admin
  (ca/conceal-value (config/from-file "./config/local-config.edn")
                    :secret/dev
                    :foodcase/bearer-token)

  (ca/conceal-value (config/from-file "./config/prod-config.edn")
                    :secret/prod
                    :foodcase/bearer-token)
  )

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
