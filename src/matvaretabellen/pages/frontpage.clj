(ns matvaretabellen.pages.frontpage
  (:require [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [mattilsynet.design :as mtds]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.seeded-random :as rng]
            [matvaretabellen.ui.search-input :refer [SearchInput]]
            [matvaretabellen.ui.toc :refer [Toc]]
            [matvaretabellen.urls :as urls])
  (:import (java.time MonthDay)))

(defn get-seasons [app-db]
  (map #(d/entity app-db %)
       (d/q '[:find [?e ...] :where [?e :season/id]] app-db)))

(defn current-season? [season md-now]
  (and (not (.isAfter md-now (:season/to-md season)))
       (not (.isBefore md-now (:season/from-md season)))))

(defn get-season-food-ids [app-db md-now]
  (->> (get-seasons app-db)
       (filter #(current-season? % md-now))
       (mapcat :season/food-ids)
       (sort)))

(defn get-food-info [locale db id]
  (if-let [food (d/entity db [:food/id id])]
    (let [food-name (get-in food [:food/name locale])]
      {:title food-name
       :href (urls/get-food-url locale food-name)})
    (prn "Sesongmatvaren med id" id "finnes ikke! Rydd opp i season-foods.edn")))

(defn TriviaBox [locale food-db trivia]
  [:div {:class (mtds/classes :grid :banner)}
   [:div {:class (mtds/classes :flex) :data-center "xl"}
    [:a {:class (mtds/classes :card :flex)
         :href (:href (get-food-info locale food-db (:trivia/food-id trivia)))
         :data-align "center"
         :data-items "200"
         :data-self "500"}
     [:img {:src (:trivia/photo trivia) :data-fixed "" :alt ""}]
     [:div {:class (mtds/classes :prose)}
      [:h2 {:class (mtds/classes :heading) :data-size "xs"} [:i18n ::did-you-know]]
      [:p (get-in trivia [:trivia/prose locale])]
      [:p [:span {:class (mtds/classes :link)}
           [:i18n ::read-more-about {:definite (get-in trivia [:trivia/definite locale])}]]]]]
    [:div {:class (mtds/classes :grid) :data-self "200"}
     [:a {:class (mtds/classes :card :prose)
          :href "https://www.mattilsynet.no/mat-og-drikke/matvaretabellen"}
      [:h2 {:class (mtds/classes :heading) :data-size "2xs"} [:i18n ::card-about-title]]
      [:p [:i18n ::card-about-text]]]
     [:a {:class (mtds/classes :card :prose)
          :href (urls/get-search-url locale)}
      [:h2 {:class (mtds/classes :heading) :data-size "2xs"} [:i18n ::card-table-title]]
      [:p [:i18n ::card-table-text]]]]]])

(def popular-search-terms
  [{:nb "Egg" :en "Egg"}
   {:nb "Banan" :en "Banana"}
   {:nb "Havregryn" :en "Oat"}
   {:nb "Gulrot" :en "Carrot"}
   {:nb "Karbohydrat" :en "Carbohydrate"}
   {:nb "Avokado" :en "Avocado"}
   {:nb "Norvegia gulost" :en "Norvegia"}
   {:nb "Agurk" :en "Cucumber"}
   {:nb "Paprika" :en "Sweet pepper"}])

(defn render [context food-db page]
  (let [locale (:page/locale page)
        app-db (:app/db context)]
    (layout/layout
     context
     page
     [:head
      [:title [:i18n :i18n/search-label]]
      [:meta {:property "og:title" :content [:i18n ::open-graph-title]}]
      [:meta {:property "og:description" :content [:i18n ::open-graph-description]}]]
     [:body {:data-size "lg"}
      [:div {:class (mtds/classes :grid) :data-gap "12"}
       (layout/render-header
        {:locale locale
         :app/config (:app/config context)}
        urls/get-base-url)
       [:form {:class (mtds/classes :grid)
               :data-center "sm"
               :action (urls/get-search-url locale)
               :method :get}
        [:h1 {:class (mtds/classes :heading) :data-size "md"} [:i18n :i18n/search-label]]
        (SearchInput {:button {:text [:i18n :i18n/search-button]}
                      :class :mvt-autocomplete
                      :input {:name "q"}
                      :autocomplete-id "foods-results"
                      :default-value [:i18n :i18n/search-default-value]})
        [:noscript
         [:div {:class (mtds/classes :alert)} [:i18n ::no-script-search-info]]
         [:img {:src "/tracer/no-script/" :height 0 :width 0}]]
        [:a {:href (urls/get-search-url (:page/locale page)) :data-size "sm"}
         [:i18n ::all-foods]]]
       (TriviaBox locale food-db
                  (rng/rand-nth*
                   (/ (.getEpochSecond (:time/instant context)) 17)
                   (map #(d/entity app-db %)
                        (d/q '[:find [?e ...] :where [?e :trivia/food-id]]
                             app-db))))
       [:div {:class (mtds/classes :grid) :data-center "xl" :data-items "300"}
        (let [new-foods (:new-foods (:page/details page))]
          (Toc {:title (list [:i18n ::new-in-food-table] " " (:year new-foods))
                :contents (->> (:food-ids new-foods)
                               (rng/shuffle* (/ (.getEpochSecond (:time/instant context)) 5))
                               (take 5)
                               (keep #(get-food-info locale food-db %)))}))
        (Toc {:title [:i18n ::seasonal-goods]
              :contents (->> (get-season-food-ids (:app/db context) (MonthDay/now))
                             (rng/shuffle* (/ (.getEpochSecond (:time/instant context)) 7))
                             (take 5)
                             (keep #(get-food-info locale food-db %)))})
        (Toc {:title [:i18n ::common-food-searches]
              :contents (->> (for [m popular-search-terms]
                               (let [term (get m locale)]
                                 {:title term
                                  :href (str "?search=" (str/lower-case term))}))
                             (rng/shuffle* (/ (.getEpochSecond (:time/instant context)) 11))
                             (take 5))})]]])))
