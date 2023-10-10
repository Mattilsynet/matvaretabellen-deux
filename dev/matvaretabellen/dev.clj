(ns matvaretabellen.dev
  (:require [clojure.data.json :as json]
            [confair.config :as config]
            [confair.config-admin :as ca]
            [courier.http :as http]
            [integrant.core :as ig]
            [integrant.repl.state]
            [matvaretabellen.core :as matvaretabellen]
            [powerpack.app :as app]))

(def config (-> (config/from-file "./config/local-config.edn")
                (config/mask-config)))

(defmethod ig/init-key :powerpack/app [_ _]
  (set! *print-namespace-maps* false)
  (matvaretabellen/create-dev-app))

(comment

  (app/start)
  (app/stop)
  (app/reset)

  integrant.repl.state/system

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
