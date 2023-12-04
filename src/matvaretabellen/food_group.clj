(ns matvaretabellen.food-group
  (:require [clojure.data.json :as json]
            [clojure.set :as set]
            [datomic-type-extensions.api :as d]
            [mmm.components.checkbox :refer [Checkbox]]))

(defn get-food-group-paths [food-groups & [path]]
  (mapcat
   (fn [group]
     (let [path (conj (vec path) (:food-group/id group))]
       (->> (get-food-group-paths (:food-group/_parent group) path)
            (cons path))))
   food-groups))

(defn render-filter-data [food-groups]
  [:script.mvt-filter-paths {:type "application/json"}
   (json/write-str (get-food-group-paths food-groups))])

(defn food-group->sort-key [app-db food-group]
  [(->> [:food-group/id (:food-group/id food-group)]
        (d/entity app-db)
        :food-group/category
        (d/entity app-db)
        :category/order)
   (:food-group/id food-group)])

(defn get-all-food-group-foods [food-group]
  (apply concat (:food/_food-group food-group)
         (map get-all-food-group-foods (:food-group/_parent food-group))))

(defn get-foods-in-group [group foods]
  (set/intersection (set foods) (set (get-all-food-group-foods group))))

(defn render-food-group-list [app-db food-groups foods locale & [{:keys [class id]}]]
  (when (seq food-groups)
    [:ul.mmm-ul.mmm-unadorned-list
     (cond-> {:class class}
       id (assoc :data-filter-list-id id))
     (->> food-groups
          (sort-by #(food-group->sort-key app-db %))
          (map (juxt identity #(count (get-foods-in-group % foods))))
          (remove (comp zero? second))
          (map (fn [[group n]]
                 [:li
                  (Checkbox
                   {:data-filter-id (:food-group/id group)
                    :label [:i18n ::food-group
                            {:food-group (get-in group [:food-group/name locale])
                             :n n}]})
                  (render-food-group-list
                   app-db
                   (:food-group/_parent group)
                   foods
                   locale
                   {:class :mmm-hidden
                    :id (:food-group/id group)})])))]))

(defn get-food-groups [foods-db]
  (->> (d/q '[:find [?e ...]
              :where
              [?e :food-group/id]
              (not [?e :food-group/parent])]
            foods-db)
       (map #(d/entity foods-db %))))

(defn render-food-group-filters [app-db food-groups foods locale]
  (list
   (render-filter-data food-groups)
   (render-food-group-list app-db food-groups (set foods) locale)))
