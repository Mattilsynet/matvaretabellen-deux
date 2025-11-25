(ns mmm.components.toc
  (:require [phosphor.icons :as icons]))

(defn Toc [{:keys [title icon contents class]}]
  [:div.mmm-toc.mmm-vert-layout-m {:class class}
   [:h3.mmm-toc-title.mmm-h3
    (when icon
      [:span.mmm-toc-icon (icons/render icon)])
    title]
   [:ul.mmm-ul
    (for [{:keys [title href contents]} contents]
      [:li
       [:a.mmm-link {:href href} title]
       (when (seq contents)
         [:ul.mmm-ul
          (for [{:keys [title href]} contents]
            [:li [:a.mmm-link {:href href} title]])])])]])
