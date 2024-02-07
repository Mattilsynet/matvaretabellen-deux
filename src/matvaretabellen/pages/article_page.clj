(ns matvaretabellen.pages.article-page
  (:require [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.urls :as urls]
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
     [:body
      (layout/render-header locale (or (:page/i18n-uris page)
                                       (constantly (:page/uri page))))
      (layout/render-toolbar
       {:locale locale
        :crumbs [{:text [:i18n :i18n/search-label]
                  :url (urls/get-base-url locale)}]})
      [:div.mmm-container-medium.mmm-section.mmm-text.mmm-vert-layout-m
       (-> (:page/body page)
           (replace-placeholders food-db locale infos)
           md/render-html)]])))
