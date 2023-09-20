(ns mt-designsystem.components.breadcrumbs)

(defn Breadcrumbs [{:keys [links]}]
  [:nav.breadcrumbs {:aria-label "breadcrumbs"}
   [:ol
    (for [{:keys [text url]} links]
      [:li (if url
             [:a.forward-arrow {:href url} text]
             [:span.last-breadcrumb text])])]])
