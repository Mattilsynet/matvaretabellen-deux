(ns matvaretabellen.pages.foodex2-term-page
  (:require [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.foodex2 :as foodex2]))

(defonce !lctx (atom nil))
(defonce !lpage (atom nil))

(defn push [term structure]
  [:pre [:code [:strong term "\n"]
         (pr-str (cond->> structure
                   (= datomic_type_extensions.entity.TypeExtendedEntityMap
                      (type structure))
                   (into {})))]])

(defn render-facet [facet]
  (list (:foodex2.facet/id facet) " " (:foodex2.facet/name facet)))

(defn render-food [food]
  (:en (:food/name food)))

(defn render [context page]
  (reset! !lctx context)
  (reset! !lpage page)
  (let [foods-db (:foods/db context)
        code (:foodex2.term/code page)
        term (d/entity foods-db [:foodex2.term/code code])]
    [:div
     [:h1 (:foodex2.term/code term) " " (:foodex2.term/name term)]
     [:p (:foodex2.term/note term)]
     (when (seq (:foodex2.term/note-links term))
       (list
        [:h2 "References"]
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
                     [:li (:en (:food/name food))])))]))

     ;; Foods with this aspect
     (for [[facet foods] (->> (foodex2/term->aspected term)
                              #_
                              (sort-by (comp (juxt :foodex2.facet/id :foodex2.facet/name)
                                             first)))]

       (list
        [:h2 (render-facet facet)]
        [:ul
         (for [food (sort-by (comp :en :food/name) foods)]
           [:li (render-food food)])]))

     #_#_#_
     (push "Page" page)
     (push "Term" term)
     (push "Map?" (map? page))
     ]))

(comment
  @!lpage

  (def foods-db (:foods/db @!lctx))
  (def term (d/entity foods-db [:foodex2.term/code "A00BL"]))

  )
