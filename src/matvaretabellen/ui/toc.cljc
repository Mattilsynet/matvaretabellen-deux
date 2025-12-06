(ns matvaretabellen.ui.toc
  (:require [phosphor.icons :as icons]
            [mattilsynet.design :as mtds]))

(defn Toc [{:keys [title icon contents class]}]
  [:div {:class (mtds/classes :grid class)}
   [:h3 {:class (mtds/classes :heading)}
    (when icon
      (icons/render icon))
    title]
   [:ul {:class (mtds/classes :grid) :data-gap "1"}
    (for [{:keys [title href contents]} contents]
      [:li
       [:a {:href href} title]
       (when (seq contents)
         [:ul {:class (mtds/classes :grid) :data-gap "1"}
          (for [{:keys [title href]} contents]
            [:li [:a {:href href} title]])])])]])
