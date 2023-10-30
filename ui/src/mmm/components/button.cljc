(ns mmm.components.button
  (:require [fontawesome.icons :as icons]))

(def sizes
  {:large "mmm-button-large"})

(defn Button [{:keys [href text inline? icon size] :as attrs}]
  [(if href :a.mmm-button.mmm-focusable :button.mmm-button.mmm-focusable)
   (cond-> (dissoc attrs :inline? :size :text)
     inline? (update :class str " mmm-button-inline")
     (sizes size) (update :class str " " (sizes size)))
   (when icon
     (icons/render icon {:class :mmm-button-icon}))
   text])
