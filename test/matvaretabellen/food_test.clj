(ns matvaretabellen.food-test
  (:require [clojure.test :refer [deftest is]]
            [matvaretabellen.food :as sut]))

(deftest humanize-langual-classification-test
  (is (= (sut/humanize-langual-classification "PASTEURIZED BY HEAT")
         "Pasteurized by heat"))

  (is (= (sut/humanize-langual-classification "Z.   ADJUNCT CHARACTERISTICS OF FOOD")
         "Z. Adjunct characteristics of food"))

  (is (= (sut/humanize-langual-classification "PROPANE-1,2-DIOL (PROPYLENE GLYCOL) ADDED")
         "Propane-1,2-diol (PROPYLENE GLYCOL) added")))
