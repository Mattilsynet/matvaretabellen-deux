(ns matvaretabellen.pages.foodex2-term-page
  (:require [datomic-type-extensions.api :as d]))

(defonce !lctx (atom nil))
(defonce !lpage (atom nil))

(defn push [term structure]
  [:pre [:code [:strong term "\n"]
         (pr-str (cond->> structure
                   (= datomic_type_extensions.entity.TypeExtendedEntityMap
                      (type structure))
                   (into {})))]])

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
