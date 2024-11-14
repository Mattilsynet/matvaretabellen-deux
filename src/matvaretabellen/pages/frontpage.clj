(ns matvaretabellen.pages.frontpage
  (:require [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.seeded-random :as rng]
            [matvaretabellen.urls :as urls]
            [mmm.components.search-input :refer [SearchInput]]
            [mmm.components.toc :refer [Toc]])
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
  [:div.mmm-banner-media-buttons.mmm-section.mmm-brand-theme2
   [:div.mmm-container
    [:div.mmm-media
     [:aside
      [:img.mvt-card-img-l {:src (:trivia/photo trivia)}]]
     [:article.mmm-text.mmm-tight.mmm-vert-layout-s
      [:h4 [:i18n ::did-you-know]]
      [:p (get-in trivia [:trivia/prose locale])]
      [:p [:a.mmm-nbr (get-food-info locale food-db (:trivia/food-id trivia))
           [:i18n ::read-more-about {:definite (get-in trivia [:trivia/definite locale])}]]]]]
    [:div.mmm-buttons.mmm-text.mmm-tight
     [:a.mmm-banner-button.mmm-vert-layout-s
      {:href (urls/get-food-groups-url locale)}
      [:p.mmm-banner-title [:i18n ::all-food-groups]]
      [:p [:i18n ::see-all-food-groups-overview]]]
     [:a.mmm-banner-button.mmm-vert-layout-s
      {:href (urls/get-nutrients-url locale)}
      [:p.mmm-banner-title [:i18n ::all-nutrients]]
      [:p [:i18n ::see-all-nutrients-overview]]]]]])

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
     [:body
      (layout/render-header
       {:locale locale
        :app/config (:app/config context)}
       urls/get-base-url)
      [:form.mmm-container-narrow.mmm-section.mmm-mbxxl.mmm-mtxl
       {:action (urls/get-search-url locale)
        :method :get}
       [:h1.mmm-h2.mmm-mbm [:i18n :i18n/search-label]]
       (SearchInput {:button {:text [:i18n :i18n/search-button]}
                     :class :mvt-autocomplete
                     :input {:name "q"}
                     :autocomplete-id "foods-results"})
       [:noscript
        [:div.mmm-card.mmm-mtm.mmm-themed [:i18n ::no-script-search-info]]
        [:img {:src "/tracer/no-script/" :height 0 :width 0}]]
       [:p.mmm-p.mmm-small.mmm-mts.mmm-desktop.mmm-js-required
        [:a {:href (urls/get-search-url (:page/locale page))}
         [:i18n ::all-foods]]]]
      (TriviaBox locale food-db
                 (rng/rand-nth*
                  (/ (.getEpochSecond (:time/instant context)) 17)
                  (map #(d/entity app-db %)
                       (d/q '[:find [?e ...] :where [?e :trivia/food-id]]
                            app-db))))
      [:div.mmm-container.mmm-section.mmm-cols
       (let [new-foods (:new-foods (:page/details page))]
         (Toc {:title (list [:i18n ::new-in-food-table] " " (:year new-foods))
               :contents (->> (:food-ids new-foods)
                              (rng/shuffle* (/ (.getEpochSecond (:time/instant context)) 5))
                              (take 5)
                              (keep #(get-food-info locale food-db %)))
               :class :mmm-col}))
       (Toc {:title [:i18n ::seasonal-goods]
             :contents (->> (get-season-food-ids (:app/db context) (MonthDay/now))
                            (rng/shuffle* (/ (.getEpochSecond (:time/instant context)) 7))
                            (take 5)
                            (keep #(get-food-info locale food-db %)))
             :class :mmm-col})
       (Toc {:title [:i18n ::common-food-searches]
             :contents (->> (for [m popular-search-terms]
                              (let [term (get m locale)]
                                {:title term
                                 :href (str "?search=" (str/lower-case term))}))
                            (rng/shuffle* (/ (.getEpochSecond (:time/instant context)) 11))
                            (take 5))
             :class :mmm-col})]])))
