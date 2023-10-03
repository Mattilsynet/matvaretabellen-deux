(ns matvaretabellen.dev
  (:require [clojure.data.json :as json]
            [confair.config :as config]
            [confair.config-admin :as ca]
            [courier.http :as http]))

(def config (-> (config/from-file "./config/local-config.edn")
                (config/mask-config)))

(comment ;; config-admin
  (ca/conceal-value (config/from-file "./config/local-config.edn")
                    :secret/dev
                    :foodcase/bearer-token)

  (ca/conceal-value (config/from-file "./config/prod-config.edn")
                    :secret/prod
                    :foodcase/bearer-token)
  )

(comment
  (set! *print-namespace-maps* false)

  (def res (http/request
            {:req {:method :get
                   :url (str (:foodcase/base-url config) "/aggregated-foods")
                   :query-params {:size "5"}
                   :headers {"Authorization" (str "Bearer " (:foodcase/bearer-token config))}
                   :accept :json}}))

  (def body (json/read-str (:body res) :key-fn keyword))

  (first (:content body))

  )
