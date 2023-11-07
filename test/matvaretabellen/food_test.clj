(ns matvaretabellen.food-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest testing is]]
            [matvaretabellen.food :as sut]))

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
                  "https://quadram.ac.uk/UKfoodcomposition/"])))))
