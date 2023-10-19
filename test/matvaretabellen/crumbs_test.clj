(ns matvaretabellen.crumbs-test
  (:require [clojure.test :refer [deftest is testing]]
            [matvaretabellen.crumbs :as sut]))

(deftest crumble
  (testing "Always includes home and search"
    (is (= (sut/crumble :nb)
           [{:text "Hjem" :url "https://www.mattilsynet.no/"}
            {:text [:i18n :breadcrumbs/search-label]}])))

  (testing "Uses custom crumbs verbatim"
    (is (= (sut/crumble :nb {:text [:i18n :breadcrumbs/all-food-groups]})
           [{:text "Hjem" :url "https://www.mattilsynet.no/"}
            {:text [:i18n :breadcrumbs/search-label] :url "/"}
            {:text [:i18n :breadcrumbs/all-food-groups]}])))

  (testing "Food groups crumble deliciously"
    (is (= (->> (sut/crumble :nb {:food-group/id :foo
                                  :food-group/name {:nb "Småkaker"}})
                (drop 2))
           [{:text [:i18n :breadcrumbs/all-food-groups]
             :url [:i18n :breadcrumbs/food-groups-url]}
            {:text "Småkaker"}]))

    (is (= (->> (sut/crumble :nb {:food-group/id :foo
                                  :food-group/name {:nb "Småkaker"}
                                  :food-group/parent {:food-group/name {:nb "Søtsaker"}}})
                (drop 3))
           [{:text "Søtsaker", :url "/gruppe/sotsaker/"}
            {:text "Småkaker"}]))))

;; ::sut/
;; fra :breadcrumbs/ -> :matvaretabellen.crumbs/
