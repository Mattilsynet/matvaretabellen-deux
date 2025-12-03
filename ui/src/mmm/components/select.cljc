;; TODO EIRIK: Not in use anymore
(ns mmm.components.select
  (:require [mattilsynet.design :as mtds]))

(defn Select [attrs]
  (into [:select {:class (mtds/classes :input)} (dissoc attrs :options :size)]
        (:options attrs))
   )
