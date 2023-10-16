(ns matvaretabellen.pages
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.components.toc :refer [Toc]]
            [matvaretabellen.search-index :as index]
            [matvaretabellen.urls :as urls]
            [mt-designsystem.components.breadcrumbs :refer [Breadcrumbs]]
            [mt-designsystem.components.search-input :refer [SearchInput]]
            [mt-designsystem.components.site-header :refer [SiteHeader]]))

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

(defn render-frontpage [_context _db page]
  [:html
   [:body
    (SiteHeader {:home-url "/"})
    [:div
     [:div.container.mtl
      (Breadcrumbs
       {:links [{:text "Mattilsynet.no" :url "https://www.mattilsynet.no/"}
                {:text [:i18n :frontpage/search-label]}]})]
     [:div.container.mtl
      [:div.search-input-wrap
       (SearchInput {:label [:i18n :frontpage/search-label]
                     :button {:text [:i18n :frontpage/search-button]}
                     :input {:name "foods-search"}
                     :autocomplete-id "foods-results"})]]]]])

(defn create-food-group-breadcrumbs [locale food-group url?]
  (let [food-group-name (get-in food-group [:food-group/name locale])]
    (concat (when-let [parent (:food-group/parent food-group)]
              (create-food-group-breadcrumbs locale parent true))
            [(cond-> {:text food-group-name}
               url? (assoc :url (urls/get-food-group-url locale food-group-name)))])))

(def top-level-breadcrumbs
  [{:text "Mattilsynet.no" :url "https://www.mattilsynet.no/"}
   {:text [:i18n :breadcrumbs/search-label] :url "/"}
   {:text [:i18n :breadcrumbs/all-food-groups] :url [:i18n :breadcrumbs/food-groups-url]}])

(defn render-food-page [context _db page]
  (let [food (d/entity (:foods/db context) [:food/id (:food/id page)])
        locale (:page/locale page)
        food-name (get-in food [:food/name locale])]
    [:html
     [:body
      (SiteHeader {:home-url "/"})
      [:div
       [:div.mvt-hero-banner
        [:div.container
         (Breadcrumbs
          {:links (concat
                   top-level-breadcrumbs
                   (create-food-group-breadcrumbs locale (:food/food-group food) true)
                   [{:text food-name}])})]]
       [:div.mvt-hero-banner
        [:div.container
         [:div {:style {:display "flex"}}
          [:div {:style {:flex "1"}}
           [:h1.h1 food-name]
           [:div.intro.mtl
            [:div [:i18n :food/food-id {:id (:food/id food)}]]
            [:div [:i18n :food/category {:category (get-in food [:food/food-group :food-group/name locale])}] ]
            [:div [:i18n :food/latin-name {:food/latin-name (:food/latin-name food)}]]]]
          (Toc {:title [:i18n :food/toc-title]
                :contents [{:title [:i18n :food/nutrition-title]
                            :href "#naeringsinnhold"
                            :contents [{:title [:i18n :food/energy-title]
                                        :href "#energi"}
                                       {:title [:i18n :food/fat-title]
                                        :href "#fett"}
                                       {:title [:i18n :food/carbohydrates-title]
                                        :href "#karbohydrater"}
                                       {:title [:i18n :food/vitamins-title]
                                        :href "#vitaminer"}
                                       {:title [:i18n :food/minerals-title]
                                        :href "#mineraler"}]}
                           {:title [:i18n :food/adi-title]
                            :href "#adi"}
                           {:title [:i18n :food/description-title]
                            :href "#beskrivelse"}]})]]]]]]))

(defn render-food-group-page [context _db page]
  (let [food-group (d/entity (:foods/db context)
                             [:food-group/id (:food-group/id page)])
        locale (:page/locale page)]
    [:html
     [:body
      (SiteHeader {:home-url "/"})
      [:div
       [:div.mvt-hero-banner
        [:div.container
         (Breadcrumbs {:links (concat top-level-breadcrumbs
                                      (create-food-group-breadcrumbs locale food-group false))})]]
       [:div.mvt-hero-banner
        [:div.container
         [:h1.h1 (get-in food-group [:food-group/name locale])]
         [:div.mvt-cards.mtl
          (for [child (:food-group/_parent food-group)]
            (let [the-name (get-in child [:food-group/name locale])]
              [:a.mvt-card {:href (urls/get-food-group-url locale the-name)}
               the-name]))]]]]]]))

(defn render-food-groups-page [context _db page]
  (let [db (:foods/db context)
        food-groups (map #(d/entity db %)
                         (d/q '[:find [?e ...]
                                :where
                                [?e :food-group/id]
                                (not [?e :food-group/parent])]
                              db))
        locale (:page/locale page)]
    [:html
     [:body
      (SiteHeader {:home-url "/"})
      [:div
       [:div.mvt-hero-banner
        [:div.container
         (Breadcrumbs {:links top-level-breadcrumbs})]]
       [:div.mvt-hero-banner
        [:div.container
         [:h1.h1 [:i18n :food-groups/all-food-groups]]
         [:div.mvt-cards.mtl
          (for [child food-groups]
            (let [the-name (get-in child [:food-group/name locale])]
              [:a.mvt-card {:href (urls/get-food-group-url locale the-name)}
               the-name]))]]]]]]))

(defn render-page [context page]
  (let [db (:foods/db context)]
    (case (:page/kind page)
      :page.kind/foods-index (render-foods-index db page)
      :page.kind/foods-lookup (render-foods-lookup db page)
      :page.kind/frontpage (render-frontpage context db page)
      :page.kind/food (render-food-page context db page)
      :page.kind/food-group (render-food-group-page context db page)
      :page.kind/food-groups (render-food-groups-page context db page))))
