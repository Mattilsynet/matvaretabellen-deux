(ns matvaretabellen.foodex2
  (:require [clojure.string :as str]))

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
                          ;; It's not entirely clear whether FoodEx2 aspects in
                          ;; classifiers *must* be sorted. But we choose to,
                          ;; because that gives us nice properties like
                          ;; roundtripping. Also, the classifiers I've seen are
                          ;; all of sorted aspects.
                          sort))))
