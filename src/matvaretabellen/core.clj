(ns matvaretabellen.core
  (:require [matvaretabellen.ingest :as ingest]
            [matvaretabellen.pages :as pages]))

(def app
  {:config
   {:site/default-language "no"
    :site/title "Matvaretabellen"

    :stasis/build-dir "build"
    :powerpack/content-dir "resources/content"
    :powerpack/source-dirs ["src" "dev"]
    :powerpack/resource-dirs ["resources"]
    :powerpack/db "datomic:mem://matvaretabellen"

    :optimus/bundles {"styles.css"
                      {:public-dir "public"
                       :paths ["/css/mt-designsystem.css"
                               "/css/matvaretabellen.css"]}}

    :powerpack.server/port 5053

    :datomic/schema-file "resources/app-schema.edn"}
   :create-ingest-tx #'ingest/create-tx
   :render-page #'pages/render-page})

(defn create-build-app []
  (assoc app :powerpack/base-url "https://matvaretabellen.mattilsynet.io"))

(defn create-dev-app []
  app)
