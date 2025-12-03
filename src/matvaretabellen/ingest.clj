(ns matvaretabellen.ingest
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.excel :as excel]
            [matvaretabellen.foodcase-import :as import]
            [matvaretabellen.foodex2 :as foodex2]
            [matvaretabellen.mashdown :as mashdown]
            [matvaretabellen.pages :as pages]
            [matvaretabellen.rda :as rda]
            [matvaretabellen.urls :as urls]))

(defn with-open-graph [m {:keys [title description image]}]
  (cond-> m
    title (assoc :open-graph/title (str title " - Matvaretabellen.no"))
    description (assoc :open-graph/description (mashdown/strip description))
    image (assoc :open-graph/image image)))

(defn get-food-pages [db]
  (->> (d/q '[:find ?food-id ?food-name
              :where
              [?f :food/id ?food-id]
              [?f :food/name ?food-name]]
            db)
       (mapcat
        (fn [[id i18n-names]]
          (for [[locale food-name] i18n-names]
            (-> {:page/uri (urls/get-food-url locale food-name)
                 :page/kind :page.kind/food
                 :page/locale locale
                 :page/food-id id}
                (with-open-graph
                  {:title food-name})))))))

(defn get-active-foodex2-term-pages
  [foods-db]
  (->> (set/union (foodex2/food-classification-terms foods-db)
                  (foodex2/food-aspect-terms foods-db))
       (mapcat (fn [[code]]
                 (map (fn [locale]
                        {:foodex2.term/code code
                         :page/kind :page.kind/foodex2-term
                         :page/locale locale
                         :page/uri (urls/get-foodex-term-url locale {:foodex2.term/code code})})
                      [:en :nb])))))

(defn get-nutrient-pages [food-db app-db]
  (->> (d/q '[:find ?nutrient-id ?nutrient-name
              :where
              [?n :nutrient/id ?nutrient-id]
              [?n :nutrient/name ?nutrient-name]]
            food-db)
       (mapcat
        (fn [[id i18n-names]]
          (mapcat
           (fn [[locale nutrient-name]]
             (let [nutrient (d/entity app-db [:nutrient/id id])]
               [(-> {:page/uri (urls/get-nutrient-url locale nutrient-name)
                     :page/kind :page.kind/nutrient
                     :page/locale locale
                     :page/nutrient-id id
                     :page/sort-order :sort.order/desc}
                    (with-open-graph
                      {:title nutrient-name
                       :description (get-in nutrient [:nutrient/short-description locale])
                       :image (:nutrient/photo nutrient)}))
                {:page/uri (urls/get-nutrient-excel-url locale nutrient-name)
                 :page/kind :page.kind/nutrient-excel
                 :page/locale locale
                 :page/nutrient-id id}]))
           i18n-names)))))

(defn get-food-group-pages [foods-db app-db]
  (->> (d/q '[:find ?id ?name
              :where
              [?f :food-group/id ?id]
              [?f :food-group/name ?name]]
            foods-db)
       (mapcat
        (fn [[id i18n-names]]
          (mapcat
           (fn [[locale name]]
             (let [food-group (d/entity app-db [:food-group/id id])]
               [(-> {:page/uri (urls/get-food-group-url locale name)
                     :page/kind :page.kind/food-group
                     :page/locale locale
                     :page/food-group-id id}
                    (with-open-graph
                      {:title name
                       :image (:food-group/photo food-group)
                       :description (get-in food-group [:food-group/short-description locale])}))
                {:page/uri (urls/get-food-group-excel-url locale name)
                 :page/kind :page.kind/food-group-excel
                 :page/locale locale
                 :page/food-group-id id}]))
           i18n-names)))))

(defn ensure-unique-page-uris [entity-maps]
  (when-not (= (count entity-maps)
               (count (set (map :page/uri entity-maps))))
    (throw (ex-info "Duplicate :page/uri detected, awooooga, awoooga!"
                    {:duplicates (->> (map :page/uri entity-maps)
                                      frequencies
                                      (remove (comp #{1} val)))})))
  entity-maps)

(defn add-page-etags [pages]
  (let [content-hash (import/get-content-hash)
        code-version (or (not-empty (System/getenv "GIT_SHA")) (System/currentTimeMillis))
        etag (str code-version "-" content-hash)
        excel-etag (str excel/version "-" content-hash)]
    (for [page pages]
      (cond-> page
        (#{:page.kind/nutrient-excel
           :page.kind/food-group-excel
           :page.kind/foods-excel} (:page/kind page))
        (assoc :page/etag excel-etag)

        (#{:page.kind/article
           :page.kind/compact-food-data
           :page.kind/comparison
           :page.kind/food
           :page.kind/food-data
           :page.kind/food-group
           :page.kind/food-group-data
           :page.kind/food-groups
           :page.kind/foods-index
           :page.kind/langual-data
           :page.kind/names-lookup
           :page.kind/nutrient
           :page.kind/nutrient-data
           :page.kind/nutrients
           :page.kind/rda-data
           :page.kind/search-page
           :page.kind/source-data} (:page/kind page))
        (assoc :page/etag etag)))))

(defn on-started [foods-conn powerpack-app]
  (let [food-db (d/db foods-conn)
        app-db (d/db (:datomic/conn powerpack-app))
        rda-profiles (rda/read-csv food-db (slurp (io/file "data/adi.csv")))]
    (->> (concat (pages/get-static-pages)
                 (get-food-pages food-db)
                 (get-food-group-pages food-db app-db)
                 (get-nutrient-pages food-db app-db)
                 (get-active-foodex2-term-pages food-db))
         add-page-etags
         (ensure-unique-page-uris)
         (concat rda-profiles)
         (d/transact (:datomic/conn powerpack-app))
         deref)))

(defn create-tx [_file-name datas]
  datas)
