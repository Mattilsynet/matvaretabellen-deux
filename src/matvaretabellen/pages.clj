(ns matvaretabellen.pages
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.search-index :as index]
            [mt-designsystem.components.search-input :refer [SearchInput]]
            [mt-designsystem.components.site-header :refer [SiteHeader]]
            [powerpack.html :as html]))

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

(defn render-frontpage [context _db page]
  (html/render-hiccup
   context
   page
   (list
    (SiteHeader {:home-url "/"})
    [:div.container
     [:div.search-input-wrap
      (SearchInput {:label "Søk i Matvaretabellen"
                    :button {:text "Søk"}
                    :input {:name "foods-search"}
                    :autocomplete-id "foods-results"})]])))

(defn render-food-page [context db page]
  (let [food (d/entity (:foods/db context) [:food/id (:food/id page)])]
    (html/render-hiccup
     context
     page
     (list
      (SiteHeader {:home-url "/"})
      [:div.container
       [:h1 (get-in food [:food/name (:page/locale page)])]]))))

(defn render-page [context page]
  (let [db (:foods/db context)]
    (case (:page/kind page)
      :page.kind/foods-index (render-foods-index db page)
      :page.kind/foods-lookup (render-foods-lookup db page)
      :page.kind/frontpage (render-frontpage context db page)
      :page.kind/food (render-food-page context db page)
      ))
  )
