(ns matvaretabellen.i18n-test
  (:require [clojure.test :refer [deftest is testing]]
            [matvaretabellen.i18n :as sut]))

(deftest format-number-test
  (testing "Rounds to the desired number of decimals"
    (is (= (sut/format-number :nb 1295.670427 {:decimals 2}) "1 295,67")))

  (testing "Always rounds whole numbers to 0 decimals"
    (is (= (sut/format-number :nb 1295.0 {:decimals 2}) "1 295")))

  (testing "Always skips 0 as the ending decimal"
    (is (= (sut/format-number :nb 1295.10 {:decimals 2}) "1 295,1")))

  (testing "Uses two decimals by default"
    (is (= (sut/format-number :nb 1295.670427) "1 295,67")))

  (testing "Uses english formatting"
    (is (= (sut/format-number :en 1295.670427) "1,295.67"))))

(deftest enumerate-test
  (testing "Enumerates in norwegian"
    (is (= (sut/enumerate :nb ["Banan" "Eple" "Kake"])
           "Banan, Eple og Kake")))

  (testing "Enumerates in english"
    (is (= (sut/enumerate :en ["Banan" "Eple" "Kake"])
           "Banan, Eple and Kake")))

  (testing "Enumerates nothing"
    (is (= (sut/enumerate :en []) "")))

  (testing "Enumerates a single word"
    (is (= (sut/enumerate :en ["Banan"]) "Banan"))))
