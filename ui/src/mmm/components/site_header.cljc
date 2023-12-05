(ns mmm.components.site-header
  (:require [mmm.components.mattilsynet-logo :refer [MattilsynetLogoSimple]]))

(defn SiteHeader [{:keys [home-url extra-link extra-links]}]
  [:header.mmm-header
   [:div.mmm-container {:style {:justify-content "space-between"}}
    [:a.mmm-fc {:href home-url}
     (MattilsynetLogoSimple {:class :mmm-svg})]
    (when extra-link
      [:a.mmm-link {:href (:url extra-link)}
       (:text extra-link)])
    (when extra-links
      [:ul.mmm-horizontal-list.mmm-horizontal-list-wide
       (for [{:keys [url text class]} extra-links]
         [:li {:class class}
          (if url
            [:a.mmm-link {:href url} text]
            text)])])]])
