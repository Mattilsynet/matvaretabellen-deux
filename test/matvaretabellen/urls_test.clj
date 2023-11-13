(ns matvaretabellen.urls-test
  (:require [clojure.test :refer [deftest testing is]]
            [matvaretabellen.urls :as sut]))

(deftest get-url-test
  (testing "Slugifies text"
    (is (= (sut/get-url :nb "gruppe/" "Brød og kornvarer")
           "/gruppe/brod-og-kornvarer/")))

  (testing "Prefixes english URLs"
    (is (= (sut/get-url :en nil "Carbohydrate")
           "/en/carbohydrate/")))

  (testing "Never ends URL in dash"
    (is (= (sut/get-url :en nil "Carbohydrate (Lol!)")
           "/en/carbohydrate-lol/"))))

(deftest get-food-url
  (is (= (sut/get-food-url :nb "Banan, rå")
         "/banan-ra/"))

  (is (= (sut/get-food-url :en "Banana, raw")
         "/en/banana-raw/")))

(deftest get-food-group-url
  (is (= (sut/get-food-group-url :nb "Frukt og bær, rå/fersk")
         "/gruppe/frukt-og-baer-ra-fersk/"))

  (is (= (sut/get-food-group-url :en "Fruit and berries, raw/fresh")
         "/en/group/fruit-and-berries-raw-fresh/"))

  (is (= (sut/get-food-group-excel-url :nb "Frukt og bær, rå/fersk")
         "/gruppe/frukt-og-baer-ra-fersk.xlsx")))
