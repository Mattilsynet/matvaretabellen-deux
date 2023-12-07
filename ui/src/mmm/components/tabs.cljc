(ns mmm.components.tabs)

(defn Tabs [{:keys [tabs] :as attrs}]
  [:div.mmm-tabs (dissoc attrs :tabs)
   (for [{:keys [selected? text] :as tab} tabs]
     [:div.tab
      (-> (dissoc tab :selected? :text)
          (assoc :class (when selected? :selected)))
      text])])

(defn PillTabs [params]
  (Tabs (assoc params :class [:mmm-tabs-pill-mode])))
