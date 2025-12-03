(ns mmm.components.site-header
  (:require [mattilsynet.design :as mtds]))

(defn SiteHeader [{:keys [home-url extra-link extra-links]}]
  [:header.header
   [:div {:class (mtds/classes :flex) :data-center "xl" :data-justify "space-between" :data-align "center"}
    [:a {:class (mtds/classes :logo)
         :href home-url}
     "Matvaretabellen"]
    (when extra-link
      [:a {:href (:url extra-link)}
       (:text extra-link)])
    (when extra-links
      [:menu {:class (mtds/classes :flex) :data-gap "8"}
       (for [{:keys [url text class]} extra-links]
         [:li {:class class}
          (if url
            [:a {:href url} text]
            text)])])]])
