(ns matvaretabellen.pages.food-page-test
  (:require [clojure.test :refer [deftest is testing]]
            [matvaretabellen.misc :as misc]
            [matvaretabellen.pages.food-page :as sut]))

(deftest calculable-quantites-test
  (testing "Rounds to the desired number of decimals"
    (is (= (sut/get-calculable-quantity
            {:measurement/quantity (misc/kilojoules 1295.670427)}
            {:decimals 2})
           '([:span {:data-portion "1295.67"
                     :data-decimals "2"}
              "1295.67"] " " [:span.mvt-sym "kJ"]))))

  (testing "Always rounds whole numbers to 0 decimals"
    (is (= (sut/get-calculable-quantity
            {:measurement/quantity (misc/kilojoules 1295.0)}
            {:decimals 2})
           '([:span {:data-portion "1295"
                     :data-decimals "2"}
              "1295"] " " [:span.mvt-sym "kJ"]))))

  (testing "Defaults to 1 decimal"
    (is (= (sut/get-calculable-quantity
            {:measurement/quantity (misc/kilojoules 1295.670427)})
           '([:span {:data-portion "1295.7"} "1295.7"] " " [:span.mvt-sym "kJ"])))))

(deftest energy-test
  (testing "Controls kJ decimals"
    (is (= (sut/energy
            {:food/energy
             {:measurement/quantity (misc/kilojoules 1234.56)}})
           '([:span {:data-portion "1235"
                     :data-decimals "0"
                     :class "mvt-kj"} "1235"] " " [:span.mvt-sym "kJ"]))))

  (testing "Controls kcal decimals"
    (is (= (sut/energy
            {:food/energy
             {:measurement/quantity (misc/kilojoules 1234.56)}
             :food/calories
             {:measurement/observation "234"}})
           '([:span {:data-portion "1235"
                     :data-decimals "0"
                     :class "mvt-kj"} "1235"]
             " " [:span.mvt-sym "kJ"]
             " (" [:span {:data-portion "234"
                          :data-decimals "0"
                          :class "mvt-kcal"} "234"]
             " kcal" ")")))))
