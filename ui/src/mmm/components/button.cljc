;; TODO EIRIK: Not in use anymore
(ns mmm.components.button
  (:require [phosphor.icons :as icons]
            [mattilsynet.design :as mtds]))

(def sizes
  {:large "lg"
   :small "sm"})

(defn Button [{:keys [href text inline? secondary? icon icon-position size] :as attrs}]
  [(if href :a :button)
   (cond-> (dissoc attrs :inline? :size :text :secondary? :icon :icon-position)
     true (update :class conj (mtds/classes :button))
     secondary? (assoc :data-variant "secondary")
     (not secondary?) (assoc :data-variant "primary")
     (sizes size) (assoc :data-size (sizes size)))
   (when (and icon (not= :after icon-position))
     (icons/render icon))
   [:span text]
   (when (and icon (= :after icon-position))
     (icons/render icon))])
