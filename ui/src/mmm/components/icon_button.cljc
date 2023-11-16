(ns mmm.components.icon-button
  (:require [fontawesome.icons :as icons]))

(defn IconButton [{:keys [icon] :as attrs}]
  [:span.mmm-icon-button.mmm-actionable.mmm-mobile
   (dissoc attrs :icon)
   (icons/render icon {:class :mmm-svg})])
