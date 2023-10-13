(ns matvaretabellen.components.toc)

(def info-icon
  [:svg {:viewBox "0 0 30 30"
         :fill "none"
         :xmlns "http://www.w3.org/2000/svg"}
   [:circle {:cx "15" :cy "15" :r "14" :fill "#348BA3" :stroke "#348BA3" :stroke-width "2"}]
   [:path {:d "M16.5 11.4751V22.5H13.5V11.4751H16.5ZM16.5 7.5V10.1601H13.5V7.5H16.5Z" :fill "white"}]])

(defn Toc [{:keys [title contents]}]
  [:div.toc
   [:div.toc-title
    [:div.icon info-icon]
    [:h3.h3 title]]
   [:ol
    (for [{:keys [title href contents]} contents]
      [:li
       [:a.no-underline {:href href} title]
       (when (seq contents)
         [:ol
          (for [{:keys [title href]} contents]
            [:li [:a.no-underline {:href href} title]])])])]])
