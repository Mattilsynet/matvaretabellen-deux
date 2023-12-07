(ns mmm.components.tabs)

(defn Tabs [{:keys [tabs class]}]
  [:div.mmm-tabs {:class class}
   (for [{:keys [selected? text id]} tabs]
     [:div.tab
      {:class (when selected? :selected)
       :id id}
      text])])

(defn PillTabs [params]
  (Tabs (assoc params :class [:mmm-tabs-pill-mode])))
