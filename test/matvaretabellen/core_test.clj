(ns matvaretabellen.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [matvaretabellen.core :as sut]))

(deftest format-number-test
  (testing "Rounds to the desired number of decimals"
    (is (= (sut/format-number :nb 1295.670427 {:decimals 2}) "1 295,67")))

  (testing "Always rounds whole numbers to 0 decimals"
    (is (= (sut/format-number :nb 1295.0 {:decimals 2}) "1 295")))

  (testing "Defaults to 1 decimal"
    (is (= (sut/format-number :nb 1295.670427) "1 295,67")))

  (testing "Uses english formatting"
    (is (= (sut/format-number :en 1295.670427) "1,295.67"))))
