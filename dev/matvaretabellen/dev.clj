(ns matvaretabellen.dev
  (:require [clojure.data.json :as json]
            [clojure.tools.namespace.repl :as repl]
            [confair.config :as config]
            [confair.config-admin :as ca]
            [courier.http :as http]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.core :as matvaretabellen]
            [matvaretabellen.export :as export]
            [matvaretabellen.foodcase-import :as foodcase-import]
            [matvaretabellen.misc :as misc]
            [matvaretabellen.search-index :as index]
            [powerpack.dev :as dev :refer [start reset]]
            [snitch.core]))

(defn load-local-config []
  (-> (config/from-file "./config/local-config.edn")
      (config/mask-config)))

(defmethod dev/configure! :default []
  (set! *print-namespace-maps* false)
  (repl/set-refresh-dirs "src" "dev" "test" "ui/src")
  (matvaretabellen/create-dev-app (load-local-config)))

(comment

  (def app-db (d/db (:datomic/conn (dev/get-app))))
  (def config (load-local-config))
  (def conn (d/connect (:foods/datomic-uri config)))

  ;; If your database is empty ("Could not find foods in catalog")
  ;; Remember to `make start-transactor`
  (time
   (foodcase-import/create-database-from-scratch (:foods/datomic-uri config)))

  (start)
  (reset)

  (export/export)

  (d/q '[:find [(pull ?p [:nutrient/id :nutrient/name]) ...]
         :where
         [?p :nutrient/id]]
       (d/db conn))

  (set
   (d/q '[:find ?id ?d
          :where
          [?r :rda/demographic ?d]
          [?r :rda/id ?id]]
        app-db))

  (misc/summarize-food (d/entity (d/db conn) [:food/id "05.448"]))

  (def constituent
    (->> (d/entity (d/db conn) [:food/id "05.448"])
         :food/constituents
         (drop 3)
         first))

  (misc/->map (get recommendations (:nutrient/id (:constituent/nutrient constituent))))
  (misc/->map constituent)
  (misc/->map recommendations)
  (misc/->map rda-profile)

  (def recommendations (->> (:rda/recommendations rda-profile)
                            (map (juxt :rda.recommendation/nutrient-id identity))
                            (into {})))

  (def rda-profile (d/entity app-db [:rda/id 6]))

  (index/index-foods {} (d/db conn) :en)

  (seq (d/datoms (d/db (:datomic/conn (dev/get-app)))
                 :avet :page/uri))

  ;; config-admin
  (ca/conceal-value (config/from-file "./config/local-config.edn")
                    :secret/dev
                    :foodcase/bearer-token)

  (ca/conceal-value (config/from-file "./config/prod-config.edn")
                    :secret/prod
                    :foodcase/bearer-token))

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
