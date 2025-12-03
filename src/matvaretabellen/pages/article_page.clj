(ns matvaretabellen.pages.article-page
  (:require [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [mattilsynet.design :as mtds]
            [matvaretabellen.layout :as layout]
            [powerpack.markdown :as md]))

(defn get-update-date [locale infos]
  (str (get-in infos [:month locale])
       " " (:year infos)))

(defn replace-placeholders [s food-db locale infos]
  (-> s
      (str/replace #"\{\{:num-food-items\}\}"
                   (str (d/q '[:find (count ?e) .
                               :where [?e :food/id]] food-db)))
      (str/replace #"\{\{:update-date\}\}"
                   (get-update-date locale infos))))

(defn render-page [context food-db page infos]
  (let [locale (:page/locale page)
        _app-db (:app/db context)]
    (layout/layout
     context
     page
     [:head
      [:title (:page/title page)]]
     [:body {:data-size "lg"}
      [:div {:class (mtds/classes :grid) :data-gap "12"}
       (layout/render-header {:locale locale
                              :app/config (:app/config context)}
                             (or (:page/i18n-uris page)
                                 (constantly (:page/uri page))))
       (layout/render-toolbar
        {:locale locale
         :crumbs []})
       [:div {:class (mtds/classes :grid) :data-center "md"}
        [:div {:class (mtds/classes :prose)}
         (-> (:page/body page)
             (replace-placeholders food-db locale infos)
             md/render-html)]]]])))
