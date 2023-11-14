(ns matvaretabellen.core
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.foodcase-import :as foodcase-import]
            [matvaretabellen.ingest :as ingest]
            [matvaretabellen.pages :as pages])
  (:import (java.text NumberFormat)
           (java.time Instant)
           (java.util Locale)))

(defn on-started [conn powerpack-app]
  (ingest/on-started conn powerpack-app))

(defn get-context [foods-conn]
  {:foods/db (d/db foods-conn)
   :time/instant (Instant/now)
   :matomo/site-id "7"})

(def locales
  {:nb (Locale/forLanguageTag "nb-NO")
   :en (Locale/forLanguageTag "en-GB")})

(defn format-number [locale n & [{:keys [decimals]}]]
  (let [formatter (NumberFormat/getNumberInstance (locales locale))]
    (when decimals
      (.setMaximumFractionDigits formatter decimals))
    (.format formatter n)))

(defn m1p-fn-num [{:keys [locale]} _params n & [opt]]
  (format-number locale n opt))

(defn create-app [env foods-conn]
  (cond-> {:site/default-locale :no
           :site/title "Matvaretabellen"

           :datomic/uri "datomic:mem://matvaretabellen"
           :datomic/schema-file "resources/app-schema.edn"

           :optimus/assets [{:public-dir "public"
                             :paths [#"/images/*.*"]}]

           :optimus/bundles {"styles.css"
                             {:public-dir "public"
                              :paths ["/css/theme-mt90s.css"
                                      "/css/mmm.css"
                                      "/css/matvaretabellen.css"]}

                             "/app.js"
                             {:public-dir "public"
                              :paths ["/js/compiled/app.js"]}}

           :powerpack/build-dir "docker/build"
           :powerpack/content-dir "resources/content"
           :powerpack/source-dirs ["src" "ui/src" "dev"]
           :powerpack/resource-dirs ["resources" "ui/resources"]
           :powerpack/port 5053
           :powerpack/log-level :debug
           :powerpack/create-ingest-tx #'ingest/create-tx
           :powerpack/render-page #'pages/render-page
           :powerpack/get-context #(get-context foods-conn)
           :powerpack/on-started #(on-started foods-conn %)

           :m1p/dictionaries {:nb ["src/matvaretabellen/i18n/nb.edn"]
                              :en ["src/matvaretabellen/i18n/en.edn"]}
           :m1p/dictionary-fns {:fn/num #'m1p-fn-num}}
    (= :prod env)
    (assoc :site/base-url "https://matvaretabellen.mattilsynet.io")

    (= :dev env) ;; serve figwheel compiled js
    (assoc :powerpack/dev-assets-root-path "public")))

(defn create-build-app []
  (let [uri "datomic:mem://foods-export"]
    (foodcase-import/create-database-from-scratch uri)
    (create-app :prod (d/connect uri))))

(defn create-dev-app [config]
  (create-app :dev (d/connect (:foods/datomic-uri config))))

(comment
  (create-build-app)
  )
