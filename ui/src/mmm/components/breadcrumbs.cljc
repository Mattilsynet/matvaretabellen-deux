(ns mmm.components.breadcrumbs
  (:require [phosphor.icons :as icons]))

(def arrow #?(:cljs (icons/icon :phosphor.regular/caret-right)
              :clj :phosphor.regular/caret-right))

(defn Breadcrumbs [{:keys [links]}]
  [:nav.mmm-breadcrumbs {:aria-label "breadcrumbs"}
   [:ol.mmm-horizontal-list
    (for [{:keys [text url]} links]
     [:li (if url
            [:span.mmm-breadcrumb
             [:a.mmm-link {:href url} text]
             (icons/render arrow {:size 12})]
            text)])]])
