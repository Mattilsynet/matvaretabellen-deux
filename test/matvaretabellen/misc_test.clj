(ns matvaretabellen.misc-test
  (:require [broch.core :as b]
            [clojure.test :refer [deftest is testing]]
            [matvaretabellen.misc :as sut]))

(deftest convert-to-readable-unit-test
  (testing "Converts tiny quantities to smaller units"
    (is (= (sut/convert-to-readable-unit (b/milligrams 0.1234))
           #broch/quantity[123.4 "µg"])))

  (testing "Converts very tiny quantities several levels"
    (is (= (sut/convert-to-readable-unit (b/milligrams 0.0001234))
           #broch/quantity[123.4 "ng"])))

  (testing "Converts big quantities to bigger units"
    (is (= (sut/convert-to-readable-unit (b/milligrams 1234))
           #broch/quantity[1.234 "g"])))

  (testing "Converts huge quantities several levels"
    (is (= (sut/convert-to-readable-unit (b/micrograms 1234123))
           #broch/quantity[1.234123 "g"]))))
