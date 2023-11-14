(ns matvaretabellen.pages.food-page-test
  (:require [clojure.test :refer [deftest is testing]]
            [matvaretabellen.misc :as misc]
            [matvaretabellen.pages.food-page :as sut]))

(deftest calculable-quantites-test
  (testing "Rounds to the desired number of decimals"
    (is (= (sut/get-calculable-quantity
            {:measurement/quantity (misc/kilojoules 1295.670427)}
            {:decimals 2})
           '([:span {:data-portion "1295.670427"
                     :data-decimals "2"}
              [:i18n :i18n/number {:n 1295.670427, :decimals 2}]] " "
             [:span.mvt-sym "kJ"]))))

  (testing "Always rounds whole numbers to 0 decimals"
    (is (= (sut/get-calculable-quantity
            {:measurement/quantity (misc/kilojoules 1295.0)}
            {:decimals 2})
           '([:span {:data-portion "1295.0"
                     :data-decimals "2"}
              [:i18n :i18n/number {:n 1295.0, :decimals 0}]] " "
             [:span.mvt-sym "kJ"]))))

  (testing "Defaults to 1 decimal"
    (is (= (sut/get-calculable-quantity
            {:measurement/quantity (misc/kilojoules 1295.670427)})
           '([:span {:data-portion "1295.670427"}
              [:i18n :i18n/number {:n 1295.670427, :decimals 1}]] " "
             [:span.mvt-sym "kJ"])))))

(deftest energy-test
  (testing "Controls kJ decimals"
    (is (= (sut/energy
            {:food/energy
             {:measurement/quantity (misc/kilojoules 1234.56)}})
           '([:span {:data-portion "1234.56"
                     :data-decimals "0"
                     :class "mvt-kj"}
              [:i18n :i18n/number {:n 1234.56, :decimals 0}]] " "
             [:span.mvt-sym "kJ"]))))

  (testing "Controls kcal decimals"
    (is (= (sut/energy
            {:food/energy
             {:measurement/quantity (misc/kilojoules 1234.56)}
             :food/calories
             {:measurement/observation "234"}})
           '([:span {:data-portion "1234.56"
                     :data-decimals "0"
                     :class "mvt-kj"}
              [:i18n :i18n/number {:n 1234.56, :decimals 0}]] " "
             [:span.mvt-sym "kJ"]
             " (" [:span {:data-portion "234"
                          :data-decimals "0"
                          :class "mvt-kcal"}
                   [:i18n :i18n/number {:n 234, :decimals 0}]]
             " kcal" ")")))))
