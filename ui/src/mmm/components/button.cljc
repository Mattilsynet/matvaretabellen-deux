(ns mmm.components.button
  (:require [fontawesome.icons :as icons]))

(def sizes
  {:large :mmm-button-large
   :small :mmm-button-small})

(defn Button [{:keys [href text inline? secondary? icon icon-position size] :as attrs}]
  [(if href :a.mmm-button.mmm-focusable :button.mmm-button.mmm-focusable)
   (cond-> (dissoc attrs :inline? :size :text :secondary? :icon :icon-position)
     inline? (update :class conj :mmm-button-inline)
     secondary? (update :class conj :mmm-button-secondary)
     (sizes size) (update :class conj (sizes size)))
   (when (and icon (not= :after icon-position))
     (icons/render icon {:class :mmm-button-icon}))
   [:span text]
   (when (and icon (= :after icon-position))
     (icons/render icon {:class :mmm-button-icon}))])
