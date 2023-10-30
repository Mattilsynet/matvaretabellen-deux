(ns mt-designsystem.components.button
  (:require [fontawesome.icons :as icons]))

(def sizes
  {:large "mvt-button-large"})

(defn button [{:keys [href text inline? icon size] :as attrs}]
  [(if href :a.mvt-button :button.mvt-button)
   (cond-> (dissoc attrs :href :inline? :size)
     inline? (update :class str " mvt-button-inline")
     (sizes size) (update :class str " " (sizes size)))
   (when icon
     (icons/render icon {:class :mvt-button-icon}))
   text])
