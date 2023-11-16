(ns mmm.components.tabs)

(defn Tabs [{:keys [tabs]}]
  [:div.mmm-tabs
   (for [{:keys [selected? text id]} tabs]
     [:div.tab
      {:class (when selected? :selected)
       :id id}
      text])])
