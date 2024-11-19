(ns matvaretabellen.pages.api-test
  (:require [clojure.test :refer [deftest is testing]]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.faux-food-db :as fdb]
            [matvaretabellen.pages.api :as sut]))

(deftest prepare-api-payload-test
  (testing "Returns \"unjoined\" EDN"
    (is (= (->> [:food/id "05.383"]
                (d/entity (fdb/get-test-food-db))
                (sut/food->api-data :nb))
           {:page/uri "/kikertmel/"
            :food/id "05.383"
            :food/name "Kikertmel"
            :food/latin-name "Cicer arietinum L."
            :food-group/id "5.1"
            :food/search-keywords #{"mel" "kikert"}
            :food/langual-codes #{"H0138" "M0001" "N0001" "F0003" "A0813"
                                  "P0024" "B1172" "G0003" "A0149" "C0155"
                                  "E0106" "K0003" "J0116"}
            :food/edible-part {:measurement/percent 100
                               :source/id "0"}
            :food/energy {:quantity/number 1581.1
                          :quantity/unit "kJ"
                          :source/id "MI0114"}
            :food/calories {:quantity/number 375
                            :quantity/unit "kcal"
                            :source/id "MI0115"}
            :food/portions #{{:portion-kind/name "porsjon"
                              :portion-kind/unit "stk"
                              :quantity/number 95.0
                              :quantity/unit "g"}
                             {:portion-kind/name "desiliter"
                              :portion-kind/unit "dl"
                              :quantity/number 60.0
                              :quantity/unit "g"}}
            :food/constituents
            #{{:nutrient/id "Enumet"
               :quantity/number 1.0
               :quantity/unit "g"
               :source/id "450c"}
              {:nutrient/id "Vit B2"
               :quantity/number 0.17
               :quantity/unit "mg"
               :source/id "450c"}
              {:nutrient/id "Fett"
               :quantity/number 5.4
               :quantity/unit "g"
               :source/id "450c"}
              {:nutrient/id "Vit C"
               :quantity/number 0.0
               :quantity/unit "mg"
               :source/id "60a"}
              {:nutrient/id "Vit E"
               :quantity/number 2.5
               :quantity/unit "mg-ATE"
               :source/id "450c"}
              {:source/id "10"
               :nutrient/id "I"}
              {:nutrient/id "Protein"
               :quantity/number 22.7
               :quantity/unit "g"
               :source/id "450c"}
              {:nutrient/id "Niacin"
               :quantity/number 1.9
               :quantity/unit "mg"
               :source/id "450c"}
              {:nutrient/id "Vit D"
               :quantity/number 0.0
               :quantity/unit "µg"
               :source/id "450c"}
              {:nutrient/id "C22:6n-3Dokosaheksaensyre"
               :quantity/number 0.0
               :quantity/unit "g"
               :source/id "30"}
              {:nutrient/id "K"
               :quantity/number 297.0
               :quantity/unit "mg"
               :source/id "450c"}
              {:nutrient/id "C20:3n-3Eikosatriensyre"
               :quantity/number 0.0
               :quantity/unit "g"
               :source/id "30"}
              {:nutrient/id "Retinol"
               :quantity/number 0.0
               :quantity/unit "µg"
               :source/id "50"}
              {:nutrient/id "B-karo"
               :quantity/number 32.0
               :quantity/unit "µg"
               :source/id "450c"}
              {:nutrient/id "C20:5n-3Eikosapentaensyre"
               :quantity/number 0.0
               :quantity/unit "g"
               :source/id "30"}
              {:nutrient/id "Vit B6"
               :quantity/number 0.45
               :quantity/unit "mg"
               :source/id "450c"}
              {:nutrient/id "Na"
               :quantity/number 2.0
               :quantity/unit "mg"
               :source/id "450c"}
              {:nutrient/id "NaCl"
               :quantity/number 0.0
               :quantity/unit "g"
               :source/id "MI0120"}
              {:nutrient/id "Omega-6"
               :quantity/number 2.4
               :quantity/unit "g"
               :source/id "30"}
              {:nutrient/id "C20:3n-6DihomoGammaLinolensyre"
               :quantity/number 0.0
               :quantity/unit "g"
               :source/id "30"}
              {:nutrient/id "Vit B1"
               :quantity/number 0.45
               :quantity/unit "mg"
               :source/id "450c"}
              {:nutrient/id "C18:3n-3AlfaLinolensyre"
               :quantity/number 0.09
               :quantity/unit "g"
               :source/id "30"}
              {:nutrient/id "Vann"
               :quantity/number 8.0
               :quantity/unit "g"
               :source/id "MI0142"}
              {:nutrient/id "C22:5n-3Dokosapentaensyre"
               :quantity/number 0.0
               :quantity/unit "g"
               :source/id "30"}
              {:nutrient/id "Vit B12"
               :quantity/number 0.0
               :quantity/unit "µg"
               :source/id "50"}
              {:nutrient/id "Sukker"
               :quantity/number 0.0
               :quantity/unit "g"
               :source/id "50"}
              {:nutrient/id "Mono+Di"
               :quantity/number 2.3
               :quantity/unit "g"
               :source/id "MI_SUGAR_NO"}
              {:nutrient/id "C12:0Laurinsyre"
               :quantity/number 0.0
               :quantity/unit "g"
               :source/id "30"}
              {:nutrient/id "Vit A RE"
               :quantity/number 5
               :quantity/unit "µg-RE"
               :source/id "MI0322"}
              {:nutrient/id "Vit A"
               :quantity/number 3.0
               :quantity/unit "µg-RE"
               :source/id "MI0325"}
              {:nutrient/id "C14:0Myristinsyre"
               :quantity/number 0.01
               :quantity/unit "g"
               :source/id "30"}
              {:nutrient/id "Stivel"
               :quantity/number 51.5
               :quantity/unit "g"
               :source/id "331"}
              {:nutrient/id "Omega-3"
               :quantity/number 0.09
               :quantity/unit "g"
               :source/id "30"}
              {:nutrient/id "C18:2n-6Linolsyre"
               :quantity/number 2.4
               :quantity/unit "g"
               :source/id "30"}
              {:nutrient/id "C20:4n-6Arakidonsyre"
               :quantity/number 0.0
               :quantity/unit "g"
               :source/id "30"}
              {:source/id "450c"
               :quantity/number 10.0
               :quantity/unit "g"
               :nutrient/id "Fiber"}
              {:source/id "30"
               :quantity/number 1.25
               :quantity/unit "g"
               :nutrient/id "C18:1"}
              {:source/id "450c"
               :quantity/number 0.0
               :quantity/unit "g"
               :nutrient/id "Trans"}
              {:source/id "30"
               :quantity/number 0.08
               :quantity/unit "g"
               :nutrient/id "C18:0Stearinsyre"}
              {:source/id "30"
               :quantity/number 0.0
               :quantity/unit "g"
               :nutrient/id "C20:4n-3Eikosatetraensyre"}
              {:source/id "50"
               :quantity/number 0.0
               :quantity/unit "mg"
               :nutrient/id "Kolest"}
              {:source/id "450c"
               :quantity/number 2.8
               :quantity/unit "g"
               :nutrient/id "Flerum"}
              {:source/id "50"
               :quantity/number 0.0
               :quantity/unit "g"
               :nutrient/id "Alko"}
              {:source/id "MI0181"
               :quantity/number 53.8
               :quantity/unit "g"
               :nutrient/id "Karbo"}
              {:source/id "450c"
               :quantity/number 0.6
               :quantity/unit "g"
               :nutrient/id "Mettet"}
              {:source/id "30"
               :quantity/number 0.01
               :quantity/unit "g"
               :nutrient/id "C16:1"}
              {:source/id "450c"
               :quantity/number 4.0
               :quantity/unit "µg"
               :nutrient/id "Se"}
              {:source/id "450c"
               :quantity/number 1.5
               :quantity/unit "mg"
               :nutrient/id "Zn"}
              {:source/id "450c"
               :quantity/number 2.6
               :quantity/unit "mg"
               :nutrient/id "Fe"}
              {:source/id "450c"
               :quantity/number 220.0
               :quantity/unit "mg"
               :nutrient/id "P"}
              {:source/id "450c"
               :quantity/number 0.27
               :quantity/unit "mg"
               :nutrient/id "Cu"}
              {:source/id "30"
               :quantity/number 0.46
               :quantity/unit "g"
               :nutrient/id "C16:0Palmitinsyre"}
              {:source/id "450c"
               :quantity/number 193.0
               :quantity/unit "µg"
               :nutrient/id "Folat"}
              {:source/id "450c"
               :quantity/number 62.0
               :quantity/unit "mg"
               :nutrient/id "Mg"}
              {:source/id "450c"
               :quantity/number 58.0
               :quantity/unit "mg"
               :nutrient/id "Ca"}}}))))

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

(deftest nutrients-api-test
  (testing "Returns EDN data"
    (is (= (-> (sut/render-nutrient-data
                {:foods/db (fdb/get-test-food-db)
                 :powerpack/app {:site/base-url "https://mvt.no"}}
                {:page/format :edn
                 :page/locale :nb})
               (update-in [:body :nutrients] #(take 4 %)))
           {:content-type :edn,
            :body
            {:nutrients
             [{:page/uri "https://mvt.no/fett/"
               :nutrient/id "Fett"
               :nutrient/name "Fett"
               :nutrient/euro-fir-id "FAT"
               :nutrient/euro-fir-name "fat, total"
               :nutrient/unit "g"
               :nutrient/decimal-precision 1}
              {:page/uri "https://mvt.no/karbohydrat/"
               :nutrient/id "Karbo"
               :nutrient/name "Karbohydrat"
               :nutrient/euro-fir-id "CHO"
               :nutrient/euro-fir-name "carbohydrate"
               :nutrient/unit "g"
               :nutrient/decimal-precision 1}
              {:page/uri "https://mvt.no/kostfiber/"
               :nutrient/id "Fiber"
               :nutrient/name "Kostfiber"
               :nutrient/euro-fir-id "FIBT"
               :nutrient/euro-fir-name "fibre, total dietary"
               :nutrient/unit "g"
               :nutrient/decimal-precision 1}
              {:page/uri "https://mvt.no/stivelse/"
               :nutrient/id "Stivel"
               :nutrient/name "Stivelse"
               :nutrient/euro-fir-id "STARCH"
               :nutrient/euro-fir-name "starch, total"
               :nutrient/unit "g"
               :nutrient/decimal-precision 1
               :nutrient/parent-id "Karbo"}]
             :locale :nb}})))

  (testing "Returns JSON data"
    (is (= (-> (sut/render-nutrient-data
                {:foods/db (fdb/get-test-food-db)
                 :powerpack/app {:site/base-url "https://mvt.no"}}
                {:page/format :json
                 :page/locale :nb})
               (update-in [:body :nutrients] #(take 4 %)))
           {:content-type :json
            :body
            {:nutrients
             [{:uri "https://mvt.no/fett/"
               :nutrientId "Fett"
               :name "Fett"
               :euroFirId "FAT"
               :euroFirName "fat, total"
               :unit "g"
               :decimalPrecision 1}
              {:uri "https://mvt.no/karbohydrat/"
               :nutrientId "Karbo"
               :name "Karbohydrat"
               :euroFirId "CHO"
               :euroFirName "carbohydrate"
               :unit "g"
               :decimalPrecision 1}
              {:uri "https://mvt.no/kostfiber/"
               :nutrientId "Fiber"
               :name "Kostfiber"
               :euroFirId "FIBT"
               :euroFirName "fibre, total dietary"
               :unit "g"
               :decimalPrecision 1}
              {:uri "https://mvt.no/stivelse/"
               :nutrientId "Stivel"
               :name "Stivelse"
               :euroFirId "STARCH"
               :euroFirName "starch, total"
               :unit "g"
               :decimalPrecision 1
               :parentId "Karbo"}]
             :locale :nb}}))))

(deftest langual-codes-api-test
  (testing "Returns EDN data"
    (is (= (-> (sut/render-langual-data
                {:foods/db (fdb/get-test-food-db)
                 :powerpack/app {:site/base-url "https://mvt.no"}}
                {:page/format :edn})
               (update-in [:body :codes] #(take 3 %)))
           {:content-type :edn
            :body
            {:codes
             [{:langual-code/id "0"
               :langual-code/description "Langual thesaurus root"}
              {:langual-code/id "A0001"
               :langual-code/description "Product type, not known"}
              {:langual-code/id "A0004"
               :langual-code/description "Product type, other"}]}})))

  (testing "Returns JSON data"
    (is (= (-> (sut/render-langual-data
                {:foods/db (fdb/get-test-food-db)
                 :powerpack/app {:site/base-url "https://mvt.no"}}
                {:page/format :json})
               (update-in [:body :codes] #(take 3 %)))
           {:content-type :json
            :body
            {:codes
             [{:langualCode "0"
               :description "Langual thesaurus root"}
              {:langualCode "A0001"
               :description "Product type, not known"}
              {:langualCode "A0004"
               :description "Product type, other"}]}}))))

(deftest sources-api-test
  (testing "Returns EDN data"
    (is (= (-> (sut/render-source-data
                {:foods/db (fdb/get-test-food-db)
                 :powerpack/app {:site/base-url "https://mvt.no"}}
                {:page/format :edn
                 :page/locale :nb})
               (update-in [:body :sources] #(take 3 %)))
           {:content-type :edn
            :body
            {:locale :nb
             :sources
             [{:source/id "0"
               :source/description "Vurdert som 100 % spiselig (netto)."}
              {:source/id "10"
               :source/description "Manglende verdi, ukjent innhold."}
              {:source/id "100"
               :source/description "Data levert av industrien 1992-2000, uspesifisert grunnlag."}]}})))

  (testing "Returns JSON data"
    (is (= (-> (sut/render-source-data
                {:foods/db (fdb/get-test-food-db)
                 :powerpack/app {:site/base-url "https://mvt.no"}}
                {:page/format :json
                 :page/locale :nb})
               (update-in [:body :sources] #(take 3 %)))
           {:content-type :json
            :body {:locale :nb
                   :sources
                   [{:sourceId "0"
                     :description "Vurdert som 100 % spiselig (netto)."}
                    {:sourceId "10"
                     :description "Manglende verdi, ukjent innhold."}
                    {:sourceId "100"
                     :description "Data levert av industrien 1992-2000, uspesifisert grunnlag."}]}}))))

(deftest food-groups-api-test
  (testing "Returns EDN data"
    (is (= (-> (sut/render-food-group-data
                {:foods/db (fdb/get-test-food-db)
                 :powerpack/app {:site/base-url "https://mvt.no"}}
                {:page/format :edn
                 :page/locale :nb})
               (update-in [:body :food-groups] #(take 3 (drop 14 %))))
           {:content-type :edn
            :body
            {:food-groups
             [{:food-group/id "15"
               :food-group/name "Poteter"}
              {:food-group/id "16"
               :food-group/name "Urter og krydder"}
              {:food-group/id "1.1"
               :food-group/name "Melk"
               :food-group/parent-id "1"}]
             :locale :nb}})))

  (testing "Returns JSON data"
    (is (= (-> (sut/render-food-group-data
                {:foods/db (fdb/get-test-food-db)
                 :powerpack/app {:site/base-url "https://mvt.no"}}
                {:page/format :json
                 :page/locale :nb})
               (update-in [:body :foodGroups] #(take 3 (drop 14 %))))
           {:content-type :json
            :body {:foodGroups
                   [{:foodGroupId "15"
                     :name "Poteter"}
                    {:foodGroupId "16"
                     :name "Urter og krydder"}
                    {:foodGroupId "1.1"
                     :name "Melk"
                     :parentId "1"}]
                   :locale :nb}}))))
