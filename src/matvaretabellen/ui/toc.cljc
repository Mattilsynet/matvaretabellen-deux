(ns matvaretabellen.ui.toc
  (:require [mattilsynet.design :as m]
            [phosphor.icons :as icons]))

(defn Toc [{:keys [title icon contents class]}]
  [:div {:class (m/c :grid class)
         :data-align-content "start"}
   [:h3 {:class (m/c :heading)}
    (when icon
      (icons/render icon))
    title]
   [:ul {:class (m/c :grid) :data-gap "1"}
    (for [{:keys [title href contents]} contents]
      [:li
       [:a {:href href} title]
       (when (seq contents)
         [:ul {:class (m/c :grid) :data-gap "1"}
          (for [{:keys [title href]} contents]
            [:li [:a {:href href} title]])])])]])
