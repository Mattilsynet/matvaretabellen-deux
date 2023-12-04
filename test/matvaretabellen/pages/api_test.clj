(ns matvaretabellen.pages.api-test
  (:require [clojure.test :refer [deftest is testing]]
            [matvaretabellen.faux-food-db :as fdb]
            [matvaretabellen.pages.api :as sut]))

(deftest food-api-data-test
  (testing "Returns food in EDN format"
    (is (= (-> (sut/render-food-data
                {:foods/db (fdb/get-test-food-db)
                 :powerpack/app {:site/base-url "https://mvt.no"}}
                {:page/format :edn
                 :page/locale :nb})
               (update-in [:body :foods] vec)
               (update-in [:body :foods 0 :food/constituents] #(set (take 3 (sort-by :nutrient/id %)))))
           {:content-type :edn
            :body
            {:foods
             [{
               :food/id "05.383"
               :food-group/id "5.1"
               :page/uri "https://mvt.no/kikertmel/"
               :food/name "Kikertmel"
               :food/latin-name "Cicer arietinum L."
               :food/calories {:source/id "MI0115"
                               :quantity/number 375
                               :quantity/unit "kcal"}
               :food/energy {:source/id "MI0114"
                             :quantity/number 1581.1
                             :quantity/unit "kJ"}
               :food/langual-codes #{"N0001" "G0003" "M0001" "K0003" "E0106" "B1172" "F0003"
                                     "C0155" "H0138" "J0116" "A0149" "P0024" "A0813"}
               :food/constituents #{{:source/id "450c"
                                     :quantity/number 32.0
                                     :quantity/unit "µg"
                                     :nutrient/id "B-karo"}
                                    {:source/id "30"
                                     :quantity/number 0.0
                                     :quantity/unit "g"
                                     :nutrient/id "C12:0Laurinsyre"}
                                    {:source/id "50"
                                     :quantity/number 0.0
                                     :quantity/unit "g"
                                     :nutrient/id "Alko"}}
               :food/edible-part {:measurement/percent 100
                                  :source/id "0"}
               :food/portions #{{:portion-kind/name "desiliter"
                                 :portion-kind/unit "dl"
                                 :quantity/number 60.0
                                 :quantity/unit "g"}
                                {:portion-kind/name "porsjon"
                                 :portion-kind/unit "stk"
                                 :quantity/number 95.0
                                 :quantity/unit "g"}}
               :food/search-keywords #{"mel" "kikert"}}]
             :locale :nb}})))

  (testing "Returns food in JSON format"
    (is (= (-> (sut/render-food-data
                {:foods/db (fdb/get-test-food-db)
                 :powerpack/app {:site/base-url "https://mvt.no"}}
                {:page/format :json
                 :page/locale :nb})
               (update-in [:body :foods] vec)
               (update-in [:body :foods 0 :constituents] #(set (take 3 (sort-by :nutrientId %)))))
           {:content-type :json
            :body
            {:foods
             [{:foodId "05.383"
               :foodGroupId "5.1"
               :uri "https://mvt.no/kikertmel/"
               :foodName "Kikertmel"
               :energy {:sourceId "MI0114"
                        :quantity 1581.1
                        :unit "kJ"}
               :calories {:sourceId "MI0115"
                          :quantity 375
                          :unit "kcal"}
               :ediblePart {:percent 100
                            :sourceId "0"}
               :constituents #{{:sourceId "450c"
                                :quantity 32.
                                :unit "µg"
                                :nutrientId "B-karo"}
                               {:sourceId "30"
                                :quantity 0.0
                                :unit "g"
                                :nutrientId "C12:0Laurinsyre"}
                               {:sourceId "50"
                                :quantity 0.0
                                :unit "g"
                                :nutrientId "Alko"}}
               :searchKeywords #{"mel" "kikert"}
               :latinName "Cicer arietinum L."
               :langualCodes #{"N0001" "G0003" "M0001" "K0003" "E0106" "B1172" "F0003"
                               "C0155" "H0138" "J0116" "A0149" "P0024" "A0813"}
               :portions #{{:portionName "porsjon"
                            :portionUnit "stk"
                            :quantity 95.0
                            :unit "g"}
                           {:portionName "desiliter"
                            :portionUnit "dl"
                            :quantity 60.0
                            :unit "g"}}}]
             :locale :nb}}))))
