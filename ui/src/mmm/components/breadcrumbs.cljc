(ns mmm.components.breadcrumbs
  (:require [mattilsynet.design :as mtds]))

;; TODO EIRIK: All breadcrumbs should have url
(defn Breadcrumbs [{:keys [links]}]
  [:nav {:class (mtds/classes :breadcrumbs) :aria-label "Du er her:" :data-size "sm"}
   [:ol
    (for [{:keys [text url]} links]
     [:li
      [:a {:href url} text]])]])
