(ns matvaretabellen.core
  (:require [datomic-type-extensions.api :as d]
            [m1p.core :as m1p]
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
    :powerpack/port 5053
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
   :on-started #(on-started foods-conn %)
   :i18n/dictionaries
   {:nb (m1p/prepare-dictionary
         {:frontpage/search-label "Søk i Matvaretabellen"
          :frontpage/search-button "Søk"

          :breadcrumbs/search-label "Søk i Matvaretabellen"
          :breadcrumbs/all-food-groups "Alle matvaregrupper"
          :breadcrumbs/food-groups-url "/matvaregrupper/"

          :food/food-id [:fn/str "Matvare-ID: {{:id}}"]
          :food/category [:fn/str "Kategori: {{:category}}"]
          :food/latin-name [:fn/str "Latin: {{:food/latin-name}}"]
          :food/toc-title "Innhold"
          :food/nutrition-title "Næringsinnhold"
          :food/energy-title "Sammensetning og energiinnhold"
          :food/fat-title "Fettsyrer"
          :food/carbohydrates-title "Karbohydrater"
          :food/vitamins-title "Vitaminer"
          :food/minerals-title "Mineraler"
          :food/adi-title "Anbefalt daglig inntak (ADI)"
          :food/description-title "Beskrivelse av matvaren"

          :food-groups/all-food-groups "Alle matvaregrupper"})

    :en (m1p/prepare-dictionary
         {:frontpage/search-label "Search in Matvaretabellen"
          :frontpage/search-button "Search"

          :breadcrumbs/search-label "Search in Matvaretabellen"
          :breadcrumbs/all-food-groups "All Food Groups"
          :breadcrumbs/food-groups-url "/food-groups/"

          :food/food-id [:fn/str "Food ID: {{:id}}"]
          :food/category [:fn/str "Category: {{:category}}"]
          :food/latin-name [:fn/str "Latin: {{:food/latin-name}}"]
          :food/toc-title "Contents"
          :food/nutrition-title "Nutritional Information"
          :food/energy-title "Composition and Energy Content"
          :food/fat-title "Fatty Acids"
          :food/carbohydrates-title "Carbohydrates"
          :food/vitamins-title "Vitamins"
          :food/minerals-title "Minerals"
          :food/adi-title "Recommended Daily Intake (ADI)"
          :food/description-title "Description of the Food Item"

          :food-groups/all-food-groups "All Food Groups"})}})

(defn ^:to-be-continued create-build-app []
  (let [uri "datomic:mem://foods-export"
        conn (d/connect uri)]
    (foodcase-import/create-database-from-scratch uri)
    (create-app :prod conn)))

(defn create-dev-app [config]
  (create-app :dev (d/connect (:foods/datomic-uri config))))
