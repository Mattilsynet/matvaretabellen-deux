(ns matvaretabellen.components.pie-chart-test
  (:require [clojure.test :refer [deftest is]]
            [matvaretabellen.components.pie-chart :as sut]))

(deftest assoc-degrees
  (is (= (sut/assoc-degrees
          0
          [{:value 10 :color "red"}
           {:value 20 :color "orange"}
           {:value 30 :color "blue"}])
         [{:from-deg 0   :to-deg 60  :value 10 :color "red"}
          {:from-deg 60  :to-deg 180 :value 20 :color "orange"}
          {:from-deg 180 :to-deg 360 :value 30 :color "blue"}])))
