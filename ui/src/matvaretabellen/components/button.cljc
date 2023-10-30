(ns matvaretabellen.components.button
  (:require [fontawesome.icons :as icons]))

(def sizes
  {:large "mvt-button-large"})

(defn button [{:keys [href text inline? icon size] :as attrs}]
  [(if href :a.mvt-button.mvt-focusable :button.mvt-button.mvt-focusable)
   (cond-> (dissoc attrs :inline? :size :text)
     inline? (update :class str " mvt-button-inline")
     (sizes size) (update :class str " " (sizes size)))
   (when icon
     (icons/render icon {:class :mvt-button-icon}))
   text])
