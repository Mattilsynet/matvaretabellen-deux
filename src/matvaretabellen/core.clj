(ns matvaretabellen.core
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.foodcase-import :as foodcase-import]
            [matvaretabellen.ingest :as ingest]
            [matvaretabellen.pages :as pages]))

(defn on-started [conn powerpack-app]
  (ingest/on-started conn powerpack-app))

(defn create-app [env foods-conn]
  {:config
   {:site/default-language "no"
    :site/title "Matvaretabellen"
    :powerpack/base-url (when (= :prod env)
                          "https://matvaretabellen.mattilsynet.io")

    :stasis/build-dir "build"
    :powerpack/content-dir "resources/content"
    :powerpack/source-dirs ["src" "ui/src" "dev"]
    :powerpack/resource-dirs ["resources" "ui/resources"]
    :powerpack/db "datomic:mem://matvaretabellen"
    :powerpack.server/port 5053
    :powerpack/dev-assets-root-path (when (= :dev env)
                                      "dev-assets")

    :optimus/bundles {"styles.css"
                      {:public-dir "public"
                       :paths ["/css/mt-designsystem.css"
                               "/css/matvaretabellen.css"]}

                      "/app.js"
                      {:public-dir (case env
                                     :prod "public"
                                     :dev "dev-assets")
                       :paths ["/js/compiled/app.js"]}}

    :imagine/config {:prefix "/imagines"}

    :datomic/schema-file "resources/app-schema.edn"}
   :create-ingest-tx #'ingest/create-tx
   :render-page #'pages/render-page
   :get-context (fn [] {:foods/db (d/db foods-conn)})
   :on-started #(on-started foods-conn %)})

(defn ^:to-be-continued create-build-app []
  (let [uri "datomic:mem://foods-export"
        conn (d/connect uri)]
    (foodcase-import/create-database-from-scratch uri)
    (create-app :prod conn)))

(defn create-dev-app [config]
  (create-app :dev (d/connect (:foods/datomic-uri config))))
