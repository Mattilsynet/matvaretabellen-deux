(ns matvaretabellen.food-test
  (:require [clojure.test :refer [deftest is testing]]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.faux-food-db :as fdb]
            [matvaretabellen.food :as sut]
            [matvaretabellen.misc :as misc]))

(deftest calculable-quantites-test
  (testing "Rounds to the desired number of decimals"
    (is (= (sut/get-calculable-quantity
            {:measurement/quantity (misc/kilojoules 1295.670427)}
            {:decimals 2})
           '([:span {:data-portion "1295.670427"
                     :data-value "1295.670427"
                     :data-decimals "2"}
              [:i18n :i18n/number {:n 1295.670427, :decimals 2}]] " "
             [:span.mvt-sym "kJ"]))))

  (testing "Always rounds whole numbers to 0 decimals"
    (is (= (sut/get-calculable-quantity
            {:measurement/quantity (misc/kilojoules 1295.0)}
            {:decimals 2})
           '([:span {:data-portion "1295.0"
                     :data-value "1295.0"
                     :data-decimals "2"}
              [:i18n :i18n/number {:n 1295.0, :decimals 0}]] " "
             [:span.mvt-sym "kJ"]))))

  (testing "Defaults to 1 decimal"
    (is (= (sut/get-calculable-quantity
            {:measurement/quantity (misc/kilojoules 1295.670427)})
           '([:span {:data-portion "1295.670427"
                     :data-value "1295.670427"}
              [:i18n :i18n/number {:n 1295.670427, :decimals 1}]] " "
             [:span.mvt-sym "kJ"])))))

(deftest humanize-langual-classification-test
  (is (= (sut/humanize-langual-classification "PASTEURIZED BY HEAT")
         "Pasteurized by heat"))

  (is (= (sut/humanize-langual-classification "Z.   ADJUNCT CHARACTERISTICS OF FOOD")
         "Z. Adjunct characteristics of food"))

  (is (= (sut/humanize-langual-classification "PROPANE-1,2-DIOL (PROPYLENE GLYCOL) ADDED")
         "Propane-1,2-diol (PROPYLENE GLYCOL) added")))

(deftest hyperlink-string-test
  (testing "Links URLs"
    (is (= (sut/hyperlink-string "Her er en lenke: https://nrk.no")
           (list "Her er en lenke: " [:a {:href "https://nrk.no"} "https://nrk.no"]))))

  (testing "Links URLs at the beginning"
    (is (= (sut/hyperlink-string "https://nrk.no er stedet å gå")
           (list [:a {:href "https://nrk.no"} "https://nrk.no"] " er stedet å gå"))))

  (testing "Links URLs in the middle"
    (is (= (sut/hyperlink-string "Du kan gå til https://nrk.no for mer")
           (list "Du kan gå til " [:a {:href "https://nrk.no"} "https://nrk.no"] " for mer"))))

  (testing "Links multiple URLs"
    (is (= (sut/hyperlink-string "Du kan gå til https://nrk.no eller https://youtube.com")
           (list "Du kan gå til "
                 [:a {:href "https://nrk.no"} "https://nrk.no"]
                 " eller "
                 [:a {:href "https://youtube.com"} "https://youtube.com"]))))

  (testing "Links multiple adjacent URLs"
    (is (= (sut/hyperlink-string "Du kan gå til https://nrk.no https://youtube.com")
           (list "Du kan gå til "
                 [:a {:href "https://nrk.no"} "https://nrk.no"]
                 " "
                 [:a {:href "https://youtube.com"} "https://youtube.com"]))))

  (testing "Shortens long URLs"
    (is (= (sut/hyperlink-string "Department of Health. Nutrient analysis of fruit and vegetables. Summary report. Department of Health, London, 2013. Nettversjon, https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/167942/Nutrient_analysis_of_fruit_and_vegetables_-_Summary_Report.pdf")
           (list "Department of Health. Nutrient analysis of fruit and vegetables. Summary report. Department of Health, London, 2013. Nettversjon, "
                 [:a {:href "https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/167942/Nutrient_analysis_of_fruit_and_vegetables_-_Summary_Report.pdf"}
                  "assets.publishing.service.gov.uk"]))))

  (testing "Breaks links away from commas"
    (is (= (sut/hyperlink-string "Food Databanks National Capability (2021). Food Databanks National Capability (FDNC) extended dataset based on PHE’s McCance and Widdowson’s Composition of Foods Integrated Dataset. Nettversjon,https://quadram.ac.uk/UKfoodcomposition/ ")
           (list "Food Databanks National Capability (2021). Food Databanks National Capability (FDNC) extended dataset based on PHE’s McCance and Widdowson’s Composition of Foods Integrated Dataset. Nettversjon, "
                 [:a
                  {:href "https://quadram.ac.uk/UKfoodcomposition/"}
                  "quadram.ac.uk"])))))

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
