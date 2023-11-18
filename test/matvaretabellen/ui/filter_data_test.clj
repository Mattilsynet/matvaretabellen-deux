(ns matvaretabellen.ui.filter-data-test
  (:require [clojure.test :refer [deftest is testing]]
            [matvaretabellen.ui.filter-data :as sut]))

(def filters
  (sut/create [["1"]
               ["1" "1.1"]
               ["1" "1.2"]
               ["1" "1.3"]
               ["1" "1.4"]
               ["1" "1.4" "1.4.1"]
               ["1" "1.4" "1.4.2"]
               ["1" "1.4" "1.4.3"]
               ["1" "1.4" "1.4.4"]
               ["2"]
               ["2" "2.1"]
               ["2" "2.2"]
               ["2" "2.3"]
               ["2" "2.4"]]))

(deftest filter-selection-test
  (testing "Selects leaf filter"
    (is (= (:selected (sut/select-id filters "1.4.1"))
           #{"1.4.1"})))

  (testing "Deselects leaf filter"
    (is (= (-> filters
               (sut/select-id "1.4.1")
               (sut/deselect-id "1.4.1")
               :selected)
           #{})))

  (testing "Deselects filter: Also deselects selected children"
    (is (= (-> filters
               (sut/select-id "1.4")
               (sut/select-id "1.4.1")
               (sut/deselect-id "1.4")
               :selected)
           #{}))))

(deftest get-active-filters-test
  (testing "Includes children of filters with no actively selected children"
    (is (= (-> filters
               (sut/select-id "1.4")
               sut/get-active)
           #{"1.4"
             "1.4.1"
             "1.4.2"
             "1.4.3"
             "1.4.4"})))

  (testing "Does not include additional children when some are selected"
    (is (= (-> filters
               (sut/select-id "1.4")
               (sut/select-id "1.4.1")
               sut/get-active)
           #{"1.4" "1.4.1"}))))
