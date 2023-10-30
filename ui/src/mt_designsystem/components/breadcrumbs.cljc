(ns mt-designsystem.components.breadcrumbs
  (:require [fontawesome.icons :as icons]))

(def arrow #?(:cljs (icons/icon :fontawesome.solid/angle-right)
              :clj :fontawesome.solid/angle-right))

(defn Breadcrumbs [{:keys [links]}]
  [:nav.mvt-breadcrumbs {:aria-label "breadcrumbs"}
   [:ol.mvt-horizontal-list
    (for [{:keys [text url]} links]
     [:li (if url
            [:span.mvt-breadcrumb
             [:a.mvt-link {:href url} text]
             (icons/render arrow {:size 12})]
            text)])]])
