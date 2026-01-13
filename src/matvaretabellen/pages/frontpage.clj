(ns matvaretabellen.pages.frontpage
  (:require [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [mattilsynet.design :as m]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.seeded-random :as rng]
            [matvaretabellen.ui.search-input :refer [SearchInput]]
            [matvaretabellen.ui.toc :refer [Toc]]
            [matvaretabellen.urls :as urls]
            [phosphor.icons :as icons])
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

(defn info-box [url icon title content]
  [:a {:class (m/c :card :flex)
       :href url}
   (icons/render icon {:size "2rem"})
   [:div {:class (m/c :prose)}
    [:h2 {:class (m/c :heading)
          :data-size "xs"}
     title]
    content]])

(defn TriviaBox [locale food-db trivia]
  [:div {:class (m/c :grid :banner)
         :data-gap "8"}
   [:div {:class (m/c :grid)
          :data-items "400"
          :data-center "xl"
          :data-column-gap "8"
          :data-row-gap "3"}
    (info-box
     (urls/get-food-groups-url locale)
     :phosphor.regular/shapes
     [:i18n ::food-groups]
     [:p [:i18n ::food-groups-more]])
    (info-box
     (urls/get-nutrients-url locale)
     :phosphor.regular/grains
     [:i18n ::all-nutrients]
     [:p [:i18n ::all-nutrients-more]])]
   [:div {:class (m/c :flex)
          :data-center "xl"}
    [:div {:class (m/c :grid)
           :data-align "center"
           :data-items "400"
           :data-gap "8"}
     [:a {:href (:href (get-food-info locale food-db (:trivia/food-id trivia)))}
      [:img {:src (:trivia/photo trivia)
             :alt ""
             :style {:width "100%"
                     :border-radius "var(--mtds-border-radius-lg)"}}]]
     [:div {:class (m/c :prose :screen-sm-inline-pad)}
      [:h2 {:class (m/c :heading) :data-size "lg"} [:i18n ::did-you-know]]
      [:p (get-in trivia [:trivia/prose locale])]
      [:p [:a {:href (:href (get-food-info locale food-db (:trivia/food-id trivia)))}
           [:i18n ::read-more-about {:definite (get-in trivia [:trivia/definite locale])}]]]]]]])

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
      [:div {:class (m/c :grid) :data-gap "8"}
       (layout/render-header
        {:locale locale
         :app/config (:app/config context)}
        urls/get-base-url)
       [:div {:class (m/c :flex)
              :data-center "xl"
              :data-items "400"
              :data-column-gap "8"
              :data-row-gap "12"}
        [:form {:class (m/c :grid)
                :action (urls/get-search-url locale)
                :method :get}
         [:h1 {:class (m/c :heading) :data-size "md"} [:i18n :i18n/search-label]]
         (SearchInput {:button {:text [:i18n :i18n/search-button]}
                       :class :mvt-autocomplete
                       :input {:name "q"}
                       :autocomplete-id "foods-results"
                       :default-value [:i18n :i18n/search-default-value]})
         [:noscript
          [:div {:class (m/c :alert)} [:i18n ::no-script-search-info]]
          [:img {:src "/tracer/no-script/" :height 0 :width 0}]]]
        [:div {:class (m/c :flex)
               :data-justify "end"
               :data-color "inverted"}
         [:a {:class (m/c :card :prose :table-box)
              :data-self "auto"
              :data-fixed ""
              :href (urls/get-search-url (:page/locale page))
              :style {:padding-right "4rem"}}
          [:h2 {:class (m/c :heading)
                :data-size "xs"}
           "Matvaretabellen"]
          [:p {:class (m/c :link)} [:i18n ::all-foods]]]]]
       (TriviaBox locale food-db
                  (rng/rand-nth*
                   (/ (.getEpochSecond (:time/instant context)) 17)
                   (map #(d/entity app-db %)
                        (d/q '[:find [?e ...] :where [?e :trivia/food-id]]
                             app-db))))
       [:div {:class (m/c :grid)
              :data-center "xl"
              :data-items "300"
              :data-column-gap "8"
              :data-row-gap "3"}
        (Toc (let [new-foods (:new-foods (:page/details page))]
               {:class (m/c :card)
                :title (list [:i18n ::new-in-food-table] " " (:year new-foods))
                :contents (->> (:food-ids new-foods)
                               (rng/shuffle* (/ (.getEpochSecond (:time/instant context)) 5))
                               (take 5)
                               (keep #(get-food-info locale food-db %)))}))
        (Toc {:class (m/c :card)
              :title [:i18n ::seasonal-goods]
              :contents (->> (get-season-food-ids (:app/db context) (MonthDay/now))
                             (rng/shuffle* (/ (.getEpochSecond (:time/instant context)) 7))
                             (take 5)
                             (keep #(get-food-info locale food-db %)))})
        (Toc {:class (m/c :card)
              :title [:i18n ::common-food-searches]
              :contents (->> (for [m popular-search-terms]
                               (let [term (get m locale)]
                                 {:title term
                                  :href (str "?search=" (str/lower-case term))}))
                             (rng/shuffle* (/ (.getEpochSecond (:time/instant context)) 11))
                             (take 5))})]]])))
