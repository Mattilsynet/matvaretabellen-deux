(ns matvaretabellen.pages.foodex2-term-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.foodex2 :as foodex2]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.urls :as urls]))

(defonce !lctx (atom nil))
(defonce !lpage (atom nil))

(defn render-facet [facet]
  (list (:foodex2.facet/id facet) " " (:foodex2.facet/name facet)))

(defn render-food-link [locale food]
  [:a {:href (urls/get-food-url locale food)}
   (get-in food [:food/name locale])])

(defn render [context page]
  (reset! !lctx context)
  (reset! !lpage page)
  (let [locale (:page/locale page)
        foods-db (:foods/db context)
        code (:foodex2.term/code page)
        term (d/entity foods-db [:foodex2.term/code code])]
    (layout/layout
     context
     page
     [:head
      [:title (:foodex2.term/code term) " " (:foodex2.term/name term)]]
     [:body
      (layout/render-header
       {:locale (:page/locale page)
        :app/config (:app/config context)}
       #(urls/get-foodex-term-url % term))
      (layout/render-toolbar
       {:locale (:page/locale page)})
      [:div
       [:h1 (:foodex2.term/code term) " " (:foodex2.term/name term)]
       [:p (:foodex2.term/note term)]
       (when (seq (:foodex2.term/note-links term))
         (list
          [:h2 (case locale
                 :nb "Referanser"
                 :en "References")]
          [:ul
           (->> (:foodex2.term/note-links term)
                (map (fn [link]
                       [:li [:a {:href link} link]])))]))

       ;; Foods classified as this
       (when-let [foods (some->> (seq (foodex2/term->classified term))
                                 (sort-by (comp :en :food/name)))]
         (list
          [:h2 "Foods classified as " (:foodex2.term/name term)]
          [:ul
           (->> foods
                (map (fn [food]
                       [:li (render-food-link locale food)])))]))

       ;; Foods with this aspect
       (for [[facet foods] (->> (foodex2/term->aspected term)
                                (sort-by (comp (juxt :foodex2.facet/id :foodex2.facet/name)
                                               first)))]
         (list
          [:h2 (render-facet facet)]
          [:ul
           (for [food (sort-by (comp (:page/locale page) :food/name) foods)]
             [:li (render-food-link locale food)])]))]])))

(comment
  @!lpage

  (def foods-db (:foods/db @!lctx))
  (def term (d/entity foods-db [:foodex2.term/code "A00BL"]))

  (-> (foodex2/term->classified term)
      first
      :food/name)

  )
