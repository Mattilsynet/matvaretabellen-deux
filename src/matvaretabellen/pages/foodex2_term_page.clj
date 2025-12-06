(ns matvaretabellen.pages.foodex2-term-page
  (:require [datomic-type-extensions.api :as d]
            [mattilsynet.design :as mtds]
            [matvaretabellen.foodex2 :as foodex2]
            [matvaretabellen.layout :as layout]
            [matvaretabellen.urls :as urls]))

(defn render-facet [facet]
  (list (:foodex2.facet/id facet) " " (:foodex2.facet/name facet)))

(defn render-food-link [locale food]
  [:a {:href (urls/get-food-url locale food)}
   (get-in food [:food/name locale])])

(defn render [context page]
  (let [locale (:page/locale page)
        foods-db (:foods/db context)
        code (:foodex2.term/code page)
        term (d/entity foods-db [:foodex2.term/code code])]
    (layout/layout
     context
     page
     [:head
      [:title (:foodex2.term/code term) " " (:foodex2.term/name term)]]
     [:body {:data-size "lg"}
      (layout/render-header
       {:locale (:page/locale page)
        :app/config (:app/config context)}
       #(urls/get-foodex-term-url % term))
      [:div {:class (mtds/classes :grid) :data-gap "12"}
       [:div {:class (mtds/classes :grid :banner) :data-gap "8" :role "banner"}
        (layout/render-toolbar
         {:locale (:page/locale page)
          :crumbs [{:text [:i18n :i18n/search-label]
                    :url (urls/get-base-url locale)}
                   {:text (:foodex2.term/code term)}]})
        [:div {:class (mtds/classes :flex)
               :data-center "xl"
               :data-align "center"}
         [:div {:class (mtds/classes :prose)
                :data-self "500"}
          [:h1 (:foodex2.term/code term) " " (:foodex2.term/name term)]
          [:p {:data-size "lg"} (:foodex2.term/note term)]]]]

       [:div {:class (mtds/classes :grid)
              :data-center "xl"}

        ;; Foods classified as this
        (when-let [foods (some->> (seq (foodex2/term->classified term))
                                  (sort-by (comp :en :food/name)))]
          [:div {:class (mtds/classes :card)}
           [:h2 [:i18n ::classified-as term]]
           [:ul
            (->> foods
                 (map (fn [food]
                        [:li (render-food-link locale food)])))]])

        ;; Foods with this aspect
        (for [[facet foods] (->> (foodex2/term->aspected term)
                                 (sort-by (comp (juxt :foodex2.facet/id :foodex2.facet/name)
                                                first)))]
          [:div {:class (mtds/classes :card)}
           [:h2 (render-facet facet)]
           [:ul
            (for [food (sort-by (comp (:page/locale page) :food/name) foods)]
              [:li (render-food-link locale food)])]])

        (when (seq (:foodex2.term/note-links term))
          [:div {:class (mtds/classes :card)}
           [:h2 [:i18n ::references]]
           [:ul
            (->> (:foodex2.term/note-links term)
                 (map (fn [link]
                        [:li [:a {:href link} link]])))]])]]])))

(comment
  (do
    (def config (matvaretabellen.dev/load-local-config))
    (def conn (d/connect (:foods/datomic-uri config)))
    (def foods-db (d/db conn)))

  (def term (d/entity foods-db [:foodex2.term/code "A00BL"]))

  (-> (foodex2/term->classified term)
      first
      :food/name)

  )
