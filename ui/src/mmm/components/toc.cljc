(ns mmm.components.toc
  (:require [fontawesome.icons :as icons]))

(defn Toc [{:keys [title icon contents class]}]
  [:div.mmm-toc.mmm-text {:class class}
   [:h3.mmm-toc-title
    (when icon
      [:span.mmm-toc-icon (icons/render icon)])
    title]
   [:ol
    (for [{:keys [title href contents]} contents]
      [:li
       [:a.no-underline {:href href} title]
       (when (seq contents)
         [:ol
          (for [{:keys [title href]} contents]
            [:li [:a.no-underline {:href href} title]])])])]])
