(ns matvaretabellen.foodex2
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [datomic-type-extensions.api :as d]))

(defn render-aspect [aspect]
  [:abbr {:title (-> aspect :foodex2/term :foodex2.term/note)}
   (str (-> aspect :foodex2/term :foodex2.term/code)
        " "
        (-> aspect :foodex2/term :foodex2.term/name))])

(defn parse-classifier [classifier]
  (let [[base-code & aspect-strs] (str/split classifier #"[#$\.]")]
    {:foodex2/term {:foodex2.term/code base-code}
     :foodex2/aspects
     (into #{}
           (map (fn [[id code]]
                  {:foodex2/facet {:foodex2.facet/id id}
                   :foodex2/term {:foodex2.term/code code}}))
           (partition 2 aspect-strs))}))

(defn make-classifier
  [foodex2]
  (str (-> foodex2 :foodex2/term :foodex2.term/code)
       "#"
       (str/join "$" (->> (:foodex2/aspects foodex2)
                          (map (fn [aspect]
                                 (str (-> aspect :foodex2/facet :foodex2.facet/id)
                                      "."
                                      (-> aspect :foodex2/term :foodex2.term/code))))
                          ;; It's not clear whether FoodEx2 requires sorted aspects.
                          ;;
                          ;; We, however, create sorted aspects.
                          ;; This gives us string-comparable classifiers.
                          sort))))

(defn term->classified
  "Foods classified as this term"
  [term]
  (->> term
       :foodex2/_term
       (keep :foodex2/_classification)))

(defn term->aspected
  "Returns {facet food} for foods that have this term as an aspect"
  [term]
  (reduce (fn [m aspect]
            (let [food (:foodex2/_classification (:foodex2/_aspects aspect))
                  facet (:foodex2/facet aspect)]
              (cond-> m
                (and food facet)
                (update facet conj food))))
          {}
          (:foodex2/_term term)))

(defn food-classification-terms [foods-db]
  (d/q '[:find ?code
         :where
         [_ :foodex2/classification ?c]
         [?c :foodex2/term ?t]
         [?t :foodex2.term/code ?code]]
       foods-db))

(defn food-aspect-terms [foods-db]
  (d/q '[:find ?code
         :where
         [_ :foodex2/classification ?c]
         [?c :foodex2/aspects ?a]
         [?a :foodex2/term ?t]
         [?t :foodex2.term/code ?code]]
       foods-db))

(comment
  ;; Julekake klassifiseres som "Bun".
  (do
    (def foods-db matvaretabellen.dev/foods-db)
    (def julekake (d/entity foods-db [:food/id "05.097"]))
    (def bun (d/entity foods-db [:foodex2.term/code "A00BL"])))

  ;; Hvilke matvarer klassifiseres som "Bun"?
  (->> bun term->classified (map (comp :nb :food/name)) sort)

  (def kandisert-appelsinskall (d/entity foods-db [:foodex2.term/code "A01QC"]))

  ;; Hvilke matvarer har appelsinskall som ingrediens?
  (-> kandisert-appelsinskall
      term->aspected
      (update-keys :foodex2.facet/name)
      (update-vals #(map (comp :nb :food/name) %)))

  (count (set/union (food-classification-terms foods-db)
                    (food-aspect-terms foods-db)))
  ;; => 1498

  (count (food-classification-terms foods-db))
  ;; => 845

  (count (food-aspect-terms foods-db))
  ;; => 925

  )
