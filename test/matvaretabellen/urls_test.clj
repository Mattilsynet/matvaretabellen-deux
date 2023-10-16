(ns matvaretabellen.urls-test
  (:require [matvaretabellen.urls :as sut]
            [clojure.test :refer [deftest is]]))

(deftest get-food-url
  (is (= (sut/get-food-url :nb "Banan, rå")
         "/banan-ra/"))

  (is (= (sut/get-food-url :en "Banana, raw")
         "/en/banana-raw/")))

(deftest get-food-group-url
  (is (= (sut/get-food-group-url :nb "Frukt og bær, rå/fersk")
         "/gruppe/frukt-og-baer-ra-fersk/"))

  (is (= (sut/get-food-group-url :en "Fruit and berries, raw/fresh")
         "/en/group/fruit-and-berries-raw-fresh/")))
