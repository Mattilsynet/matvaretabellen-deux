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
