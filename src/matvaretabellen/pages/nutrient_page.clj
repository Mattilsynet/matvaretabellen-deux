(ns matvaretabellen.pages.nutrient-page
  (:require [broch.core :as b]
            [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.crumbs :as crumbs]
            [matvaretabellen.food :as food]
            [matvaretabellen.nutrient :as nutrient]
            [matvaretabellen.pages.food-page :as food-page]
            [matvaretabellen.urls :as urls]
            [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.components.footer :refer [CompactSiteFooter]]
            [mmm.components.site-header :refer [SiteHeader]]))

(defn prepare-foods-table [nutrient locale foods]
  {:headers [{:text [:i18n ::food]}
             {:text [:i18n :i18n/lookup (nutrient/get-name nutrient)]
              :colspan 3}]
   :rows (for [food foods]
           [{:text [:a.mmm-link {:href (urls/get-food-url locale food)}
                    [:i18n :i18n/lookup (:food/name food)]]}
            {:text (->> (:nutrient/id nutrient)
                        (food/get-nutrient-measurement food)
                        :measurement/quantity
                        str)
             :class "mmm-tar"}])})

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
  (let [foods (nutrient/get-foods-by-nutrient-density nutrient)]
    (list
     [:h2.mmm-h2 [:i18n ::foods-by-nutrient
                  (str/lower-case (get (nutrient/get-name nutrient) locale))]]
     [:p.mmm-p [:i18n ::foods {:n (count foods)}]]
     (->> foods
          (prepare-foods-table nutrient locale)
          food-page/render-table))))

(defn render [context _db page]
  (let [nutrient (d/entity (:foods/db context) [:nutrient/id (:page/nutrient-id page)])
        locale (:page/locale page)
        nutrient-name (get (nutrient/get-name nutrient) locale)]
    [:html {:class "mmm"}
     [:body
      (SiteHeader {:home-url "/"})
      [:div
       [:div.mmm-themed.mmm-brand-theme1
        [:div.mmm-container.mmm-section
         (Breadcrumbs
          {:links (crumbs/crumble locale
                                  {:text [:i18n ::crumbs/search-label]
                                   :url (urls/get-base-url locale)}
                                  {:text nutrient-name})})]
        [:div.mmm-container.mmm-section
         [:div.mmm-media.mmm-media-at
          [:article.mmm-vert-layout-m.mmm-text
           [:h1 nutrient-name]
           (when-let [desc (get-in nutrient/descriptions [(:nutrient/id nutrient) locale])]
             [:p desc])]]]]

       [:div.mmm-container.mmm-section.mmm-mobile-phn
        [:div.mmm-passepartout
         (if-let [nutrients (seq (:nutrient/_parent nutrient))]
           [:div.mmm-container
            [:div.mmm-media.mmm-media-at.mmm-media-reverse
             [:article.mmm-vert-layout-m
              (render-nutrient-foods-table nutrient locale)]
             [:aside.mmm-vert-layout-m.mmm-mtxl
              {:style {:flex-basis "300px"}}
              [:h3.mmm-h3 [:i18n ::sub-groups]]
              [:ul.mmm-ul.mmm-unadorned-list
               (for [n (nutrient/sort-by-preference nutrients)]
                 [:li
                  [:a.mmm-link {:href (urls/get-nutrient-url locale n)}
                   [:i18n :i18n/lookup (nutrient/get-name n)]]])]]]]
           [:div.mmm-container-medium.mmm-vert-layout-m
            (render-nutrient-foods-table nutrient locale)])]]

       [:div.mmm-container.mmm-section
        (CompactSiteFooter)]]]]))

(comment

  (def conn matvaretabellen.dev/conn)

  (->> (d/entity (d/db conn) [:nutrient/id "Fiber"])
       nutrient/get-foods-by-nutrient-density
       (map (comp :nb :food/name))
       count)


)
