(ns matvaretabellen.pages.nutrient-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.components.comparison :as comparison]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.food :as food]
            [matvaretabellen.nutrient :as nutrient]
            [matvaretabellen.pages.food-page :as food-page]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.button :refer [Button]]
            [mmm.components.footer :refer [CompactSiteFooter]]
            [mmm.components.site-header :refer [SiteHeader]]))

(defn prepare-foods-table [nutrient locale foods]
  {:headers [{:text [:i18n ::food]}
             {:text [:i18n ::nutrient-header (nutrient/get-name nutrient)]
              :class "mmm-tar mmm-nbr"}
             {}]
   :rows (for [food foods]
           [{:text [:a.mmm-link {:href (urls/get-food-url locale food)}
                    [:i18n :i18n/lookup (:food/name food)]]}
            {:text (->> (:nutrient/id nutrient)
                        (food/get-nutrient-measurement food)
                        :measurement/quantity
                        str)
             :class "mmm-tar mmm-nbr"}
            {:text (comparison/render-toggle-button food locale)
             :class "mmm-tac mmm-pas"}])})

(defn render-nutrient-foods-table
  "Really, ALL of the foods on one page? Well, not all of them, just the ones that
  have a non-zero amount of the nutrient in question.

  The initial idea was to list a limit amount of foods - say 100. This made it
  clear that for some nutrients, food number 100 still had a high portion of
  said nutrient. So I was curious about number 101.

  Then I figured, let's have a g/100g cutoff, like ... 20? 15? 10? VERY hard to
  find a reasonable cutoff, AND we effectively only cut a few foods, while also
  making it impossible to find the food with the least amount.

  Thus: list of all foods containing the nutrient in question."
  [nutrient foods locale]
  (->> (prepare-foods-table nutrient locale foods)
       food-page/render-table))

(defn render [context _db page]
  (let [nutrient (d/entity (:foods/db context) [:nutrient/id (:page/nutrient-id page)])
        locale (:page/locale page)
        nutrient-name (get (nutrient/get-name nutrient) locale)
        foods (nutrient/get-foods-by-nutrient-density nutrient)]
    [:html {:class "mmm"}
     [:body
      (SiteHeader {:home-url "/"
                   :extra-link {:text [:i18n :i18n/other-language]
                                :url (urls/get-nutrient-url
                                      ({:en :nb :nb :en} locale)
                                      nutrient)}})
      [:div
       [:div.mmm-themed.mmm-brand-theme1
        [:div.mmm-container.mmm-section
         (Breadcrumbs
          {:links (crumbs/crumble locale
                                  {:text [:i18n ::crumbs/all-nutrients]
                                   :url (urls/get-nutrients-url locale)}
                                  {:text nutrient-name})})]
        (let [details (d/entity (:app/db context) [:nutrient/id (:nutrient/id nutrient)])
              desc (get-in details [:nutrient/long-description locale])
              illustration (:nutrient/illustration details)]
          [:div.mmm-container.mmm-section
           [:div.mmm-media
            [:article.mmm-vert-layout-m
             [:div [:h1.mmm-h1 nutrient-name]
              [:i18n :i18n/number-of-foods {:count (count foods)}]]
             (when desc
               [:div.mmm-text.mmm-preamble
                [:p desc]])
             [:div
              (Button {:text [:i18n ::download-these]
                       :href (urls/get-nutrient-excel-url locale nutrient)
                       :icon :fontawesome.solid/arrow-down
                       :inline? true
                       :secondary? true})]]
            (when (and desc illustration) ;; looks horrible without text
              [:aside.mmm-desktop {:style {:flex-basis "40%"}}
               [:img {:src illustration :width 300}]])]])]

       [:div.mmm-container-medium.mmm-section.mmm-vert-layout-m
        (render-nutrient-foods-table nutrient foods locale)]

       (comparison/render-comparison-drawer locale)

       [:div.mmm-container.mmm-section
        (CompactSiteFooter)]]]]))

(comment

  (def conn matvaretabellen.dev/conn)

  (->> (d/entity (d/db conn) [:nutrient/id "Fiber"])
       nutrient/get-foods-by-nutrient-density
       (map (comp :nb :food/name))
       count)


  )
