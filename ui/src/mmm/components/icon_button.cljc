(ns mmm.components.icon-button
  (:require [phosphor.icons :as icons]))

(defn IconButton [{:keys [icon label] :as attrs}]
  [:span.mmm-icon-button.mmm-actionable
   (dissoc attrs :icon)
   (icons/render icon {:class :mmm-svg})
   (when label
     [:span.mmm-ib-label label])])
