(ns matvaretabellen.pages
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.excel :as excel]
            [matvaretabellen.pages.api :as api]
            [matvaretabellen.pages.article-page :as article-page]
            [matvaretabellen.pages.comparison-page :as comparison-page]
            [matvaretabellen.pages.food-group-page :as food-group-page]
            [matvaretabellen.pages.food-groups-page :as food-groups-page]
            [matvaretabellen.pages.food-page :as food-page]
            [matvaretabellen.pages.frontpage :as frontpage]
            [matvaretabellen.pages.nutrient-page :as nutrient-page]
            [matvaretabellen.pages.nutrients-page :as nutrients-page]
            [matvaretabellen.pages.search-page :as search-page]
            [matvaretabellen.rda :as rda]
            [matvaretabellen.search-index :as index]
            [matvaretabellen.urls :as urls]))

(defn load-edn [file-name]
  (-> (io/file file-name)
      slurp
      edn/read-string))

(defn get-auxiliary-info []
  (load-edn "data/new-food-ids.edn"))

(defn get-latest-year []
  (:year (get-auxiliary-info)))

(defn create-api-pages [page-kind get-url locales formats]
  (for [locale locales
        format formats]
    {:page/uri (get-url locale format)
     :page/kind page-kind
     :page/locale locale
     :page/format format}))

(defn get-static-pages []
  (concat
   [{:page/uri "/"
     :page/kind :page.kind/frontpage
     :page/details {:new-foods (load-edn "data/new-food-ids.edn")}
     :page/locale :nb}
    {:page/uri "/en/"
     :page/kind :page.kind/frontpage
     :page/details {:new-foods (load-edn "data/new-food-ids.edn")}
     :page/locale :en}
    {:page/uri "/search/index/nb.json"
     :page/kind :page.kind/foods-index
     :page/locale :nb}
    {:page/uri "/search/index/en.json"
     :page/kind :page.kind/foods-index
     :page/locale :en}
    {:page/uri "/search/names/nb.json"
     :page/kind :page.kind/names-lookup
     :page/locale :nb}
    {:page/uri "/search/names/en.json"
     :page/kind :page.kind/names-lookup
     :page/locale :en}
    {:page/uri "/matvaregrupper/"
     :page/kind :page.kind/food-groups
     :page/locale :nb}
    {:page/uri "/en/food-groups/"
     :page/kind :page.kind/food-groups
     :page/locale :en}
    {:page/uri "/naeringsstoffer/"
     :page/kind :page.kind/nutrients
     :page/locale :nb}
    {:page/uri "/en/nutrients/"
     :page/kind :page.kind/nutrients
     :page/locale :en}
    {:page/uri (urls/get-foods-excel-url :nb)
     :page/kind :page.kind/foods-excel
     :page/locale :nb}
    {:page/uri (urls/get-foods-excel-url :en)
     :page/kind :page.kind/foods-excel
     :page/locale :en}
    {:page/uri (urls/get-compact-foods-json-url :nb)
     :page/kind :page.kind/compact-food-data
     :page/locale :nb}
    {:page/uri (urls/get-compact-foods-json-url :en)
     :page/kind :page.kind/compact-food-data
     :page/locale :en}
    {:page/uri (urls/get-api-rda-json-url :nb)
     :page/kind :page.kind/rda-data
     :page/locale :nb}
    {:page/uri (urls/get-api-rda-json-url :en)
     :page/kind :page.kind/rda-data
     :page/locale :en}
    {:page/uri (urls/get-comparison-url :nb)
     :page/kind :page.kind/comparison
     :page/locale :nb}
    {:page/uri (urls/get-comparison-url :en)
     :page/kind :page.kind/comparison
     :page/locale :en}
    {:page/uri (urls/get-search-url :nb)
     :page/kind :page.kind/search-page
     :page/locale :nb}
    {:page/uri (urls/get-search-url :en)
     :page/kind :page.kind/search-page
     :page/locale :en}
    {:page/uri (urls/get-langual-codes-api-url :edn)
     :page/kind :page.kind/langual-data
     :page/format :edn}
    {:page/uri (urls/get-langual-codes-api-url :json)
     :page/kind :page.kind/langual-data
     :page/format :json}]
   (create-api-pages :page.kind/food-data urls/get-foods-api-url #{:nb :en} #{:edn :json})
   (create-api-pages :page.kind/food-group-data urls/get-food-groups-api-url #{:nb :en} #{:edn :json})
   (create-api-pages :page.kind/nutrient-data urls/get-nutrients-api-url #{:nb :en} #{:edn :json})
   (create-api-pages :page.kind/source-data urls/get-sources-api-url #{:nb :en} #{:edn :json})))

(defn render-foods-index [db page]
  {:headers {"content-type" "application/json"}
   :body (index/build-index db (:page/locale page))})

(defn render-names-lookup [db page]
  {:headers {"content-type" "application/json"}
   :body (-> {}
             (into
              (for [eid (d/q '[:find [?food ...]
                               :where
                               [?food :food/id]]
                             db)]
                (let [food (d/entity db eid)]
                  [(:food/id food)
                   (get-in food [:food/name (:page/locale page)])])))
             (into
              (for [eid (d/q '[:find [?nutrient ...]
                               :where
                               [?nutrient :nutrient/id]]
                             db)]
                (let [nutrient (d/entity db eid)]
                  [(:nutrient/id nutrient)
                   (get-in nutrient [:nutrient/name (:page/locale page)])]))))})

(defn render-page [context page]
  (let [db (:foods/db context)]
    (case (:page/kind page)
      :page.kind/article (article-page/render-page context db page (get-auxiliary-info))
      :page.kind/compact-food-data (api/render-compact-foods context page)
      :page.kind/comparison (comparison-page/render-page context page)
      :page.kind/food-data (api/render-food-data context page)
      :page.kind/foods-index (render-foods-index db page)
      :page.kind/names-lookup (render-names-lookup db page)
      :page.kind/frontpage (frontpage/render context db page)
      :page.kind/food (food-page/render context db page)
      :page.kind/food-group (food-group-page/render context db page)
      :page.kind/food-groups (food-groups-page/render context db page)
      :page.kind/foods-excel (excel/render-all-foods db (get-latest-year) page)
      :page.kind/food-group-data (api/render-food-group-data context page)
      :page.kind/food-group-excel (excel/render-food-group-foods db (get-latest-year) page)
      :page.kind/langual-data (api/render-langual-data context page)
      :page.kind/nutrient-data (api/render-nutrient-data context page)
      :page.kind/nutrient-excel (excel/render-nutrient-foods db (get-latest-year) page)
      :page.kind/nutrient (nutrient-page/render context db page)
      :page.kind/nutrients (nutrients-page/render context db page)
      :page.kind/rda-data (rda/render-json context page)
      :page.kind/search-page (search-page/render context page)
      :page.kind/source-data (api/render-source-data context page))))

(comment
  (def conn matvaretabellen.dev/conn)

  (for [id (:food-ids (load-edn "data/new-food-ids.edn"))]
    [id (d/entity (d/db conn) [:food/id id])])

  )
