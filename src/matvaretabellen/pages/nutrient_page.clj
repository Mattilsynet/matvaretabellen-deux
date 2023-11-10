(ns matvaretabellen.pages.nutrient-page
  (:require [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.food :as food]
            [matvaretabellen.nutrient :as nutrient]
            [matvaretabellen.pages.food-page :as food-page]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.footer :refer [CompactSiteFooter]]
            [mmm.components.site-header :refer [SiteHeader]]))

(defn prepare-foods-table [nutrient locale foods]
  {:headers [{:text [:i18n ::food {:n (count foods)}]}
             {:text [:i18n ::nutrient-header (nutrient/get-name nutrient)]
              :class "mmm-tar mmm-nbr"}]
   :rows (for [food foods]
           [{:text [:a.mmm-link {:href (urls/get-food-url locale food)}
                    [:i18n :i18n/lookup (:food/name food)]]}
            {:text (->> (:nutrient/id nutrient)
                        (food/get-nutrient-measurement food)
                        :measurement/quantity
                        str)
             :class "mmm-tar mmm-nbr"}])})

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
  [nutrient locale]
  (->> (nutrient/get-foods-by-nutrient-density nutrient)
       (prepare-foods-table nutrient locale)
       food-page/render-table))

(defn render [context _db page]
  (let [nutrient (d/entity (:foods/db context) [:nutrient/id (:page/nutrient-id page)])
        locale (:page/locale page)
        nutrient-name (get (nutrient/get-name nutrient) locale)]
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
        [:div.mmm-container.mmm-section
         [:div.mmm-media.mmm-media-at
          [:article.mmm-vert-layout-m.mmm-text
           [:h1 nutrient-name]
           (when-let [desc (get-in nutrient/descriptions [(:nutrient/id nutrient) locale])]
             [:p desc])]]]]

       [:div.mmm-container-medium.mmm-section.mmm-vert-layout-m
        (render-nutrient-foods-table nutrient locale)]

       [:div.mmm-container.mmm-section
        (CompactSiteFooter)]]]]))

(comment

  (def conn matvaretabellen.dev/conn)

  (->> (d/entity (d/db conn) [:nutrient/id "Fiber"])
       nutrient/get-foods-by-nutrient-density
       (map (comp :nb :food/name))
       count)


  )
