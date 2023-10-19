(ns matvaretabellen.crumbs
  (:require [matvaretabellen.urls :as urls]))

(defn create-food-group-breadcrumbs [locale food-group]
  (let [food-group-name (get-in food-group [:food-group/name locale])]
    (concat (when-let [parent (:food-group/parent food-group)]
              (create-food-group-breadcrumbs locale parent))
            [{:text food-group-name
              :url (urls/get-food-group-url locale food-group-name)}])))

(defn to-crumbs [locale arg]
  (cond
    (:food-group/id arg) (concat
                          [{:text [:i18n ::all-food-groups]
                            :url [:i18n ::food-groups-url]}]
                          (create-food-group-breadcrumbs locale arg))
    :else [arg]))

(defn crumble [locale & args]
  (let [crumbs (concat
                [{:text [:i18n ::home] :url "https://www.mattilsynet.no/"}
                 {:text [:i18n ::search-label] :url "/"}]
                (mapcat #(to-crumbs locale %) args))]
    (concat (butlast crumbs)
            [(dissoc (last crumbs) :url)])))
