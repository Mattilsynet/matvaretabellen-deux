(ns matvaretabellen.pages.food-page-test
  (:require [clojure.test :refer [deftest is testing]]
            [matvaretabellen.misc :as misc]
            [matvaretabellen.pages.food-page :as sut]))

(deftest energy-test
  (testing "Controls kJ decimals"
    (is (= (sut/energy
            {:food/energy
             {:measurement/quantity (misc/kilojoules 1234.56)}})
           '([:span {:data-portion "1234.56"
                     :data-value "1234.56"
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
                     :data-value "1234.56"
                     :data-decimals "0"
                     :class "mvt-kj"}
              [:i18n :i18n/number {:n 1234.56, :decimals 0}]] " "
             [:span.mvt-sym "kJ"]
             " (" [:span {:data-portion "234"
                          :data-value "234"
                          :data-decimals "0"
                          :class "mvt-kcal"}
                   [:i18n :i18n/number {:n 234, :decimals 0}]]
             " kcal" ")")))))
