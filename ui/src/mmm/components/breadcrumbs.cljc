(ns mmm.components.breadcrumbs
  (:require [fontawesome.icons :as icons]))

(def arrow #?(:cljs (icons/icon :fontawesome.solid/angle-right)
              :clj :fontawesome.solid/angle-right))

(defn Breadcrumbs [{:keys [links]}]
  [:nav.mmm-breadcrumbs {:aria-label "breadcrumbs"}
   [:ol.mmm-horizontal-list
    (for [{:keys [text url]} links]
     [:li (if url
            [:span.mmm-breadcrumb
             [:a.mmm-link {:href url} text]
             (icons/render arrow {:size 12})]
            text)])]])
