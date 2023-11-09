(ns matvaretabellen.crumbs-test
  (:require [clojure.test :refer [deftest is testing]]
            [matvaretabellen.crumbs :as sut]))

(deftest crumble
  (testing "Uses custom crumbs verbatim"
    (is (= (sut/crumble :nb {:text [:i18n ::sut/all-food-groups]})
           [{:text [:i18n ::sut/all-food-groups]}])))

  (testing "Food groups crumble deliciously"
    (is (= (sut/crumble :nb {:food-group/id :foo
                             :food-group/name {:nb "Småkaker"}})
           [{:text [:i18n ::sut/all-food-groups]
             :url "/matvaregrupper/"}
            {:text "Småkaker"}]))

    (is (= (sut/crumble :nb {:food-group/id :foo
                             :food-group/name {:nb "Småkaker"}
                             :food-group/parent {:food-group/name {:nb "Søtsaker"}}})
           [{:text [:i18n ::sut/all-food-groups]
             :url "/matvaregrupper/"}
            {:text "Søtsaker", :url "/gruppe/sotsaker/"}
            {:text "Småkaker"}]))))
