(ns matvaretabellen.export
  (:require [confair.config :as config]
            [matvaretabellen.core :as matvaretabellen]
            [powerpack.export :as export]))

(defn load-build-config []
  (-> (config/from-file "./config/build-config.edn")
      (config/mask-config)))

(defn ^:export export [& _args]
  (set! *print-namespace-maps* false)
  (export/export! (matvaretabellen/create-build-app (load-build-config))))

(comment
  (load-build-config)
  )
