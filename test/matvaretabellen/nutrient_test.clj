(ns matvaretabellen.nutrient-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [matvaretabellen.faux-food-db :as fdb]
            [matvaretabellen.nutrient :as sut]))

(def dried-apple
  {:food/id "06.531"
   :food/calories {:measurement/observation "272"}
   :food/name {:nb "Eple tørket"}
   :food/energy {:measurement/quantity #broch/quantity[1151.8 "kJ"]}
   :food/constituents
   [{:constituent/nutrient [:nutrient/id "Kolest"]
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient [:nutrient/id "Vit B6"]
     :measurement/quantity #broch/quantity[0.13 "mg"]}
    {:constituent/nutrient [:nutrient/id "C18:3n-3AlfaLinolensyre"]
     :measurement/quantity #broch/quantity[0.04 "g"]}
    {:constituent/nutrient [:nutrient/id "Vit A"]
     :measurement/quantity #broch/quantity[8.0 "µg-RE"]}
    {:constituent/nutrient [:nutrient/id "Na"]
     :measurement/quantity #broch/quantity[16.0 "mg"]}
    {:constituent/nutrient [:nutrient/id "Fiber"]
     :measurement/quantity #broch/quantity[10.0 "g"]}
    {:constituent/nutrient [:nutrient/id "Alko"]
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient [:nutrient/id "C14:0Myristinsyre"]
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient [:nutrient/id "Ca"]
     :measurement/quantity #broch/quantity[16.0 "mg"]}
    {:constituent/nutrient [:nutrient/id "C18:2n-6Linolsyre"]
     :measurement/quantity #broch/quantity[0.21 "g"]}
    {:constituent/nutrient [:nutrient/id "Vit D"]
     :measurement/quantity #broch/quantity[0.0 "µg"]}
    {:constituent/nutrient [:nutrient/id "Folat"]
     :measurement/quantity #broch/quantity[0.0 "µg"]}
    {:constituent/nutrient [:nutrient/id "C20:4n-6Arakidonsyre"]
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient [:nutrient/id "Sukker"]
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient [:nutrient/id "C20:3n-6DihomoGammaLinolensyre"]
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient [:nutrient/id "Stivel"]
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient [:nutrient/id "Enumet"]
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient [:nutrient/id "Vit B2"]
     :measurement/quantity #broch/quantity[0.16 "mg"]}
    {:constituent/nutrient [:nutrient/id "Vit B1"]
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient [:nutrient/id "Mg"]
     :measurement/quantity #broch/quantity[16.0 "mg"]}
    {:constituent/nutrient [:nutrient/id "Retinol"]
     :measurement/quantity #broch/quantity[0.0 "µg"]}
    {:constituent/nutrient [:nutrient/id "C22:5n-3Dokosapentaensyre"]
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient [:nutrient/id "Cu"]
     :measurement/quantity #broch/quantity[0.11 "mg"]}
    {:constituent/nutrient [:nutrient/id "Mettet"]
     :measurement/quantity #broch/quantity[0.1 "g"]}
    {:constituent/nutrient [:nutrient/id "Vit E"]
     :measurement/quantity #broch/quantity[1.5 "mg-ATE"]}
    {:constituent/nutrient [:nutrient/id "B-karo"]
     :measurement/quantity #broch/quantity[91.0 "µg"]}
    {:constituent/nutrient [:nutrient/id "P"]
     :measurement/quantity #broch/quantity[43.0 "mg"]}
    {:constituent/nutrient [:nutrient/id "Vit B12"]
     :measurement/quantity #broch/quantity[0.0 "µg"]}
    {:constituent/nutrient [:nutrient/id "Niacin"]
     :measurement/quantity #broch/quantity[0.9 "mg"]}
    {:constituent/nutrient [:nutrient/id "K"]
     :measurement/quantity #broch/quantity[540.0 "mg"]}
    {:constituent/nutrient [:nutrient/id "Trans"]
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient [:nutrient/id "C16:1"]
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient [:nutrient/id "Fett"]
     :measurement/quantity #broch/quantity[0.5 "g"]}
    {:constituent/nutrient [:nutrient/id "C20:4n-3Eikosatetraensyre"]
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient [:nutrient/id "Fe"]
     :measurement/quantity #broch/quantity[0.5 "mg"]}
    {:constituent/nutrient [:nutrient/id "Omega-6"]
     :measurement/quantity #broch/quantity[0.21 "g"]}
    {:constituent/nutrient [:nutrient/id "Omega-3"]
     :measurement/quantity #broch/quantity[0.04 "g"]}
    {:constituent/nutrient [:nutrient/id "Zn"]
     :measurement/quantity #broch/quantity[0.5 "mg"]}
    {:constituent/nutrient [:nutrient/id "Flerum"]
     :measurement/quantity #broch/quantity[0.3 "g"]}
    {:constituent/nutrient [:nutrient/id "C18:0Stearinsyre"]
     :measurement/quantity #broch/quantity[0.02 "g"]}
    {:constituent/nutrient [:nutrient/id "C18:1"]
     :measurement/quantity #broch/quantity[0.03 "g"]}
    {:constituent/nutrient [:nutrient/id "Mono+Di"]
     :measurement/quantity #broch/quantity[60.1 "g"]}
    {:constituent/nutrient [:nutrient/id "C20:3n-3Eikosatriensyre"]
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient [:nutrient/id "Protein"]
     :measurement/quantity #broch/quantity[2.0 "g"]}
    {:constituent/nutrient [:nutrient/id "Se"]
     :measurement/quantity #broch/quantity[0.0 "µg"]}
    {:constituent/nutrient [:nutrient/id "C16:0Palmitinsyre"]
     :measurement/quantity #broch/quantity[0.12 "g"]}
    {:constituent/nutrient [:nutrient/id "Vit C"]
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient [:nutrient/id "C20:5n-3Eikosapentaensyre"]
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient [:nutrient/id "C22:6n-3Dokosaheksaensyre"]
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient [:nutrient/id "Vann"]
     :measurement/quantity #broch/quantity[28.0 "g"]}
    {:constituent/nutrient [:nutrient/id "NaCl"]
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient [:nutrient/id "I"]
     :measurement/quantity #broch/quantity[8.0 "µg"]}
    {:constituent/nutrient [:nutrient/id "C12:0Laurinsyre"]
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient [:nutrient/id "Karbo"]
     :measurement/quantity #broch/quantity[60.1 "g"]}]})

(defn summarize-filters [filters]
  (walk/postwalk
   (fn [x]
     (if-let [n (:sort-id x)]
       (let [options (:options x)]
         (cond-> (if (:label x) [n] [])
           (seq options) (conj options)))
       x))
   filters))

(deftest prepare-nutrient-filter-test
  (testing "Collects selectable filters with no children in one group"
    (is (= (->> (sut/create-filters (fdb/get-food-data-db [dried-apple]))
                (filter #((set (map :data-filter-id (:options %))) "Fiber"))
                first)
           {:sort-id "Fiber"
            :options
            [{:label [:i18n :i18n/lookup {:nb "Kostfiber", :en "Dietary fibre"}]
              :data-filter-id "Fiber"
              :checked? true
              :sort-id "Fiber"}
             {:label [:i18n :i18n/lookup {:nb "Protein", :en "Protein"}]
              :data-filter-id "Protein"
              :checked? true
              :sort-id "Protein"}
             {:label [:i18n :i18n/lookup {:nb "Vann", :en "Water"}]
              :data-filter-id "Vann"
              :checked? false
              :sort-id "Vann"}
             {:label [:i18n :i18n/lookup {:nb "Alkohol", :en "Alcohol"}]
              :data-filter-id "Alko"
              :checked? false
              :sort-id "Alko"}]})))

  (testing "Can't select nutrients that are not directly referred by foods"
    (is (= (->> (sut/create-filters (fdb/get-food-data-db [dried-apple]))
                (filter #((set (map :data-filter-id (:options %))) "Vit A"))
                first)
           {:label [:i18n :i18n/lookup {:nb "Fettløselige vitaminer", :en "Fat-soluble vitamins"}]
            :sort-id "FatSolubleVitamins"
            :class :mmm-h6
            :options
            [{:sort-id "Vit A"
              :label [:i18n :i18n/lookup {:nb "Vitamin A", :en "Vitamin A"}]
              :data-filter-id "Vit A"
              :checked? false
              :options
              [{:sort-id "B-karo"
                :label [:i18n :i18n/lookup {:nb "Betakaroten", :en "Beta-carotene"}],
                :data-filter-id "B-karo"
                :checked? false}
               {:sort-id "Retinol"
                :label [:i18n :i18n/lookup {:nb "Retinol", :en "Retinol"}]
                :data-filter-id "Retinol"
                :checked? false}]}
             {:sort-id "Vit D"
              :label [:i18n :i18n/lookup {:nb "Vitamin D", :en "Vitamin D"}]
              :data-filter-id "Vit D"
              :checked? false}
             {:sort-id "Vit E"
              :label [:i18n :i18n/lookup {:nb "Vitamin E", :en "Vitamin E"}]
              :data-filter-id "Vit E"
              :checked? false}]})))

  (testing "Marks top-level selectables with heading-class"
    (is (= (->> (sut/create-filters (fdb/get-food-data-db [dried-apple]))
                (filter :data-filter-id)
                (map :class))
           [:mmm-h6 :mmm-h6])))

  (testing "Prepares filters in balanced columns"
    (is (= (->> (sut/prepare-filters (fdb/get-food-data-db [dried-apple]) {:columns 2})
                summarize-filters)
           [[["Fett"
              [["Mettet"
                [["C12:0Laurinsyre"]
                 ["C14:0Myristinsyre"]
                 ["C16:0Palmitinsyre"]
                 ["C18:0Stearinsyre"]]]
               ["Trans"]
               ["Enumet" [["C16:1"] ["C18:1"]]]
               ["Flerum"
                [["C18:2n-6Linolsyre"]
                 ["C18:3n-3AlfaLinolensyre"]
                 ["C20:3n-3Eikosatriensyre"]
                 ["C20:3n-6DihomoGammaLinolensyre"]
                 ["C20:4n-3Eikosatetraensyre"]
                 ["C20:4n-6Arakidonsyre"]
                 ["C20:5n-3Eikosapentaensyre"]
                 ["C22:5n-3Dokosapentaensyre"]
                 ["C22:6n-3Dokosaheksaensyre"]]]
               ["Omega-3"]
               ["Omega-6"]
               ["Kolest"]]]
             ["Karbo"
              [["Stivel"]
               ["Mono+Di"]
               ["Sukker"]
               ["SUGAN"]]]
             [[["Fiber"]
               ["Protein"]
               ["Vann"]
               ["Alko"]]]]

            [["FatSolubleVitamins"
              [["Vit A"
                [["B-karo"]
                 ["Retinol"]]]
               ["Vit D"]
               ["Vit E"]]]
             ["WaterSolubleVitamins"
              [["Vit B1"]
               ["Vit B2"]
               ["Niacin"]
               ["NIAEQ"]
               ["Vit B6"]
               ["Folat"]
               ["Vit B12"]
               ["Vit C"]]]
             ["Minerals"
              [["Ca"]
               ["K"]
               ["Na"]
               ["NaCl"]
               ["P"]
               ["Mg"]]]
             ["TraceElements"
              [["Fe"]
               ["Cu"]
               ["Zn"]
               ["Se"]
               ["I"]]]]]))))
