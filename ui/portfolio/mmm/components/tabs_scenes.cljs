(ns mmm.components.tabs-scenes
  (:require [mmm.components.tabs :refer [Tabs PillTabs]]
            [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene two-tabs
  (e/block
   (Tabs
    {:tabs [{:text "Kakediagram" :selected? true}
            {:text "Tabell"}]})))

(defscene three-tabs
  (e/block
   (Tabs
    {:tabs [{:text "Kakediagram" :selected? true}
            {:text "Tabell"}
            {:text "Banan"}]})))

(def pill-tab-component
  (PillTabs
   {:tabs [{:text "Kakediagram" :selected? true}
           {:text "Tabell"}
           {:text "Banan"}]}))

(defscene pill-tabs
  (e/block pill-tab-component))

(defscene pill-tabs-different-themes
  [:div.mmm-block.mmm-vert-layout-m
   [:div.mmm-brand-theme2 pill-tab-component]
   [:div.mmm-brand-theme3 pill-tab-component]])
