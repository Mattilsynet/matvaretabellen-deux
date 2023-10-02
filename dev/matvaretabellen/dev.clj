(ns matvaretabellen.dev
  (:require [confair.config :as config]
            [confair.config-admin :as ca]))

(def config (-> (config/from-file "./config/local-config.edn")
                (config/mask-config)))

(comment
  (ca/conceal-value (config/from-file "./config/local-config.edn")
                    :secret/dev
                    :foodcase/bearer-token)

  (ca/conceal-value (config/from-file "./config/prod-config.edn")
                    :secret/prod
                    :foodcase/bearer-token)

  (:foodcase/bearer-token config)
  )
