(ns matvaretabellen.pages.article-page
  (:require [matvaretabellen.layout :as layout]
            [matvaretabellen.urls :as urls]
            [powerpack.markdown :as md]))

(defn render-page [context _food-db page]
  (let [locale (:page/locale page)
        _app-db (:app/db context)]
    (layout/layout
     context
     [:head
      [:title (:page/title page)]]
     [:body
      (layout/render-header locale (or (:page/i18n-uris page)
                                       (constantly (:page/uri page))))
      (layout/render-toolbar
       {:locale locale
        :crumbs [{:text [:i18n :i18n/search-label]
                  :url (urls/get-base-url locale)}]})
      [:div.mmm-container.mmm-section.mmm-text.mmm-vert-layout-m
       (md/render-html (:page/body page))]])))
