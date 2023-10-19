(ns matvaretabellen.pages
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.pages.food-group-page :as food-group-page]
            [matvaretabellen.pages.food-groups-page :as food-groups-page]
            [matvaretabellen.pages.food-page :as food-page]
            [matvaretabellen.pages.frontpage :as frontpage]
            [matvaretabellen.search-index :as index]))

(def static-pages
  [{:page/uri "/"
    :page/kind :page.kind/frontpage
    :page/locale :nb}
   {:page/uri "/en/"
    :page/kind :page.kind/frontpage
    :page/locale :en}
   {:page/uri "/index/nb.json"
    :page/kind :page.kind/foods-index
    :page/locale :nb}
   {:page/uri "/index/en.json"
    :page/kind :page.kind/foods-index
    :page/locale :en}
   {:page/uri "/foods/nb.json"
    :page/kind :page.kind/foods-lookup
    :page/locale :nb}
   {:page/uri "/foods/en.json"
    :page/kind :page.kind/foods-lookup
    :page/locale :en}
   {:page/uri "/matvaregrupper/"
    :page/kind :page.kind/food-groups
    :page/locale :nb}
   {:page/uri "/food-groups/"
    :page/kind :page.kind/food-groups
    :page/locale :en}])

(defn render-foods-index [db page]
  {:headers {"content-type" "application/json"}
   :body (index/build-index db (:page/locale page))})

(defn render-foods-lookup [db page]
  {:headers {"content-type" "application/json"}
   :body (into
          {}
          (for [eid (d/q '[:find [?food ...]
                           :where
                           [?food :food/id]]
                         db)]
            (let [food (d/entity db eid)]
              [(:food/id food)
               (get-in food [:food/name (:page/locale page)])])))})

(defn render-page [context page]
  (let [db (:foods/db context)]
    (case (:page/kind page)
      :page.kind/foods-index (render-foods-index db page)
      :page.kind/foods-lookup (render-foods-lookup db page)
      :page.kind/frontpage (frontpage/render context db page)
      :page.kind/food (food-page/render context db page)
      :page.kind/food-group (food-group-page/render context db page)
      :page.kind/food-groups (food-groups-page/render context db page))))
