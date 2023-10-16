(ns matvaretabellen.pages
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.components.toc :refer [Toc]]
            [matvaretabellen.search-index :as index]
            [matvaretabellen.urls :as urls]
            [mt-designsystem.components.breadcrumbs :refer [Breadcrumbs]]
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

(defn render-food-page [context _db page]
  (let [food (d/entity (:foods/db context) [:food/id (:food/id page)])
        locale (:page/locale page)
        food-name (get-in food [:food/name locale])
        food-group-name (get-in food [:food/food-group :food-group/name locale])]
    (html/render-hiccup
     context
     page
     (list
      (SiteHeader {:home-url "/"})
      [:div
       [:div.mvt-hero-banner
        [:div.container
         (Breadcrumbs
          {:links [{:text "Mattilsynet.no" :url "https://www.mattilsynet.no/"}
                   {:text "Søk i Matvaretabellen" :url "/"}
                   {:text "Alle matvaregrupper" :url "/"}
                   {:text food-group-name
                    :url (urls/get-food-group-url locale food-group-name)}
                   {:text food-name}]})]]
       [:div.mvt-hero-banner
        [:div.container
         [:div {:style {:display "flex"}}
          [:div {:style {:flex "1"}}
            [:h1.h1 food-name]
           [:div.intro.mtl
            [:div "Matvare-ID: " (:food/id food)]
            [:div "Kategori: " food-group-name]
            [:div "Latin: " (:food/latin-name food)]]]
          (Toc {:title "Innhold"
                :contents [{:title "Næringsinnhold"
                            :href "#naeringsinnhold"
                            :contents [{:title "Sammensetning og energiinnhold"
                                        :href "#energi"}
                                       {:title "Fettsyrer"
                                        :href "#fett"}
                                       {:title "Karbohydrater"
                                        :href "#karbohydrater"}
                                       {:title "Vitaminer"
                                        :href "#vitaminer"}
                                       {:title "Mineraler"
                                        :href "#mineraler"}]}
                           {:title "Anbefalt daglig inntak (ADI)"
                            :href "#adi"}
                           {:title "Beskrivelse av matvaren"
                            :href "#beskrivelse"}]})]]]]))))

(defn render-food-group-page [context db page]
  (let [food-group (d/entity (:foods/db context)
                             [:food-group/id (:food-group/id page)])
        locale (:page/locale page)
        food-group-name (get-in food-group [:food-group/name locale])]
    (html/render-hiccup
     context
     page
     (list
      (SiteHeader {:home-url "/"})
      [:div
       [:div.mvt-hero-banner
        [:div.container
         (Breadcrumbs {:links [{:text "Mattilsynet.no" :url "https://www.mattilsynet.no/"}
                               {:text "Søk i Matvaretabellen" :url "/"}
                               {:text "Alle matvaregrupper" :url "/"}
                               {:text food-group-name}]})]]
       [:div.mvt-hero-banner
        [:div.container
         [:div {:style {:display "flex"}}
          [:div {:style {:flex "1"}}
           [:h1.h1 food-group-name]]]]]]))))

(defn render-page [context page]
  (let [db (:foods/db context)]
    (case (:page/kind page)
      :page.kind/foods-index (render-foods-index db page)
      :page.kind/foods-lookup (render-foods-lookup db page)
      :page.kind/frontpage (render-frontpage context db page)
      :page.kind/food (render-food-page context db page)
      :page.kind/food-group (render-food-group-page context db page))))
