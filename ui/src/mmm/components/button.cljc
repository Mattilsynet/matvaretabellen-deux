(ns mmm.components.button
  (:require [fontawesome.icons :as icons]))

(def sizes
  {:large "mmm-button-large"})

(defn Button [{:keys [href text inline? secondary? icon size] :as attrs}]
  [(if href :a.mmm-button.mmm-focusable :button.mmm-button.mmm-focusable)
   (cond-> (dissoc attrs :inline? :size :text :secondary?)
     inline? (update :class str " mmm-button-inline")
     secondary? (update :class str " mmm-button-secondary")
     (sizes size) (update :class str " " (sizes size)))
   (when icon
     (icons/render icon {:class :mmm-button-icon}))
   text])
