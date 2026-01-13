(ns matvaretabellen.ui.breadcrumbs
  (:require [mattilsynet.design :as m]))

(defn Breadcrumbs [{:keys [links]}]
  [:nav {:class (m/c :breadcrumbs) :aria-label "Du er her:" :data-size "sm"}
   [:ol
    (for [{:keys [text url]} links]
      [:li
       [:a {:href url} text]])]])
