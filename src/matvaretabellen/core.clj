(ns matvaretabellen.core
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.foodcase-import :as foodcase-import]
            [matvaretabellen.ingest :as ingest]
            [matvaretabellen.pages :as pages]))

(def app
  {:config
   {:site/default-language "no"
    :site/title "Matvaretabellen"

    :stasis/build-dir "build"
    :powerpack/content-dir "resources/content"
    :powerpack/source-dirs ["src" "ui/src" "dev"]
    :powerpack/resource-dirs ["resources" "ui/resources"]
    :powerpack/db "datomic:mem://matvaretabellen"
    :powerpack.server/port 5053

    :optimus/bundles {"styles.css"
                      {:public-dir "public"
                       :paths ["/css/mt-designsystem.css"
                               "/css/matvaretabellen.css"]}}

    :imagine/config {:prefix "/imagines"}

    :datomic/schema-file "resources/app-schema.edn"}
   :create-ingest-tx #'ingest/create-tx
   :render-page #'pages/render-page})

(defn on-started [conn powerpack-app]
  (ingest/on-started conn powerpack-app))

(defn ^:to-be-continued create-build-app []
  (let [uri "datomic:mem://foods-export"
        conn (d/connect uri)]
    (foodcase-import/create-database-from-scratch uri)
    (-> app
        (assoc :powerpack/base-url "https://matvaretabellen.mattilsynet.io")
        (assoc :context {:foods/conn conn})
        (assoc :on-started #(on-started conn %)))))

(defn create-dev-app [config]
  (let [conn (d/connect (:foods/datomic-uri config))]
    (-> app
        (assoc :context {:foods/conn conn})
        (assoc :on-started #(on-started conn %)))))
