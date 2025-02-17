(ns matvaretabellen.core
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.foodcase-import :as foodcase-import]
            [matvaretabellen.i18n :as i18n]
            [matvaretabellen.ingest :as ingest]
            [matvaretabellen.pages :as pages])
  (:import (java.time Instant)))

(defn on-started [conn powerpack-app]
  (ingest/on-started conn powerpack-app))

(defn get-context [config foods-conn]
  {:app/config config
   :foods/db (d/db foods-conn)
   :time/instant (Instant/now)
   :matomo/site-id "7"})

(defn create-app [env config]
  (let [foods-conn (d/connect (:foods/datomic-uri config))]
    (cond-> {:site/default-locale :no
             :site/title "Matvaretabellen"

             :datomic/uri "datomic:mem://matvaretabellen"
             :datomic/schema-file "resources/app-schema.edn"

             :optimus/assets [{:public-dir "public"
                               :paths [#"/images/*.*"
                                       #"/fonts/*.*"
                                       "/mtds/favicon.svg"
                                       "/mtds/favicon.ico"
                                       "/mtds/favicon-dark.ico"]}]

             :optimus/bundles {"styles.css"
                               {:public-dir "public"
                                :paths (cond-> [(str "/css/theme-" (:app/theme config) ".css")
                                                "/css/mmm.css"
                                                "/css/matvaretabellen.css"]
                                         (= "mt2023" (:app/theme config))
                                         (conj "/mtds/styles.css"))}

                               "/app.js"
                               {:public-dir "public"
                                :paths ["/js/u-datalist.js"
                                        "/js/compiled/app.js"]}}

             :optimus/options {:minify-js-assets? false}

             :powerpack/build-dir "docker/build"
             :powerpack/content-dir "resources/content"
             :powerpack/source-dirs ["src" "ui/src" "dev"]
             :powerpack/resource-dirs ["resources" "ui/resources"]
             :powerpack/port 5053
             :powerpack/log-level :debug
             :powerpack/create-ingest-tx #'ingest/create-tx
             :powerpack/render-page #'pages/render-page
             :powerpack/get-context #(get-context config foods-conn)
             :powerpack/on-started #(on-started foods-conn %)
             :m1p/dictionaries {:nb ["src/matvaretabellen/i18n/nb.edn"]
                                :en ["src/matvaretabellen/i18n/en.edn"]}
             :m1p/dictionary-fns {:fn/num #'i18n/m1p-fn-num
                                  :fn/enumerate #'i18n/m1p-fn-enumerate}}
      (= :build env)
      (assoc :site/base-url "https://www.matvaretabellen.no")

      (= :dev env) ;; serve figwheel compiled js
      (assoc :powerpack/dev-assets-root-path "public"))))

(defn create-build-app [config]
  (foodcase-import/create-database-from-scratch (:foods/datomic-uri config))
  (-> (create-app :build config)
      (assoc :powerpack/log-level :info)))

(defn wrap-tracer-pages [on-started]
  (fn [powerpack-app]
    @(d/transact (:datomic/conn powerpack-app)
                 [{:page/uri "/tracer/no-script/" :page/kind :page.kind/tracer-proxy}
                  {:page/uri "/tracer/report/" :page/kind :page.kind/tracer-proxy}
                  {:page/uri "/tracer/infos/" :page/kind :page.kind/tracer-proxy}])
    (on-started powerpack-app)))

(defn create-dev-app [config]
  (-> (create-app :dev config)
      (update :powerpack/on-started wrap-tracer-pages)))
