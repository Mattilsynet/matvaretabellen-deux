(ns mt-designsystem.components.button)

(defn button [{:keys [href text inline?] :as attrs}]
  [(if href :a.mvt-button :button.mvt-button)
   (cond-> (dissoc attrs :href :inline?)
     inline? (update :class str " mvt-button-inline"))
   text])
