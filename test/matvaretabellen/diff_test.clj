(ns matvaretabellen.diff-test
  (:require [clojure.test :refer [deftest is testing]]
            [matvaretabellen.diff :as sut]))

(def medians
  {"Alko" 8.6
   "B-karo" 250.5
   "C12:0Laurinsyre" 1.0150000000000001
   "C14:0Myristinsyre" 1.32
   "C16:0Palmitinsyre" 2.91
   "C16:1" 0.625
   "C18:0Stearinsyre" 1.9049999999999998
   "C18:1" 4.82
   "C18:2n-6Linolsyre" 2.41
   "C18:3n-3AlfaLinolensyre" 0.95
   "C20:3n-3Eikosatriensyre" 0.04
   "C20:3n-6DihomoGammaLinolensyre" 0.045
   "C20:4n-3Eikosatetraensyre" 0.135
   "C20:4n-6Arakidonsyre" 0.18
   "C20:5n-3Eikosapentaensyre" 0.425
   "C22:5n-3Dokosapentaensyre" 0.18
   "C22:6n-3Dokosaheksaensyre" 0.59
   "Ca" 147.0
   "Cu" 0.635
   "Enumet" 11.8
   "Fe" 6.75
   "Fett" 19.65
   "Fiber" 15.5
   "Flerum" 7.85
   "Folat" 80.0
   "I" 72.0
   "K" 314.0
   "Karbo" 36.9
   "Kolest" 87.0
   "Mettet" 11.1
   "Mg" 95.0
   "Mono+Di" 21.799999999999997
   "Na" 347.5
   "NaCl" 4.2
   "Niacin" 5.85
   "Omega-3" 1.3849999999999998
   "Omega-6" 2.38
   "P" 216.0
   "Protein" 14.95
   "Retinol" 154.0
   "Se" 36.0
   "Stivel" 28.65
   "Sukker" 21.2
   "Trans" 1.0
   "Vann" 50.0
   "Vit A" 177.5
   "Vit B1" 0.545
   "Vit B12" 5.3
   "Vit B2" 0.58
   "Vit B6" 0.55
   "Vit C" 57.0
   "Vit D" 6.1
   "Vit E" 8.350000000000001
   "Zn" 3.9})

(def dried-apple
  {:food/id "06.531"
   :food/calories {:measurement/observation "272"}
   :food/name {:nb "Eple tørket"}
   :food/energy {:measurement/quantity #broch/quantity[1151.8 "kJ"]}
   :food/constituents
   [{:constituent/nutrient {:nutrient/id "Kolest" :nutrient/parent "Fett"}
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient
     {:nutrient/id "Vit B6" :nutrient/parent "WaterSolubleVitamins"}
     :measurement/quantity #broch/quantity[0.13 "mg"]}
    {:constituent/nutrient
     {:nutrient/id "C18:3n-3AlfaLinolensyre" :nutrient/parent "Flerum"}
     :measurement/quantity #broch/quantity[0.04 "g"]}
    {:constituent/nutrient
     {:nutrient/id "Vit A" :nutrient/parent "FatSolubleVitamins"}
     :measurement/quantity #broch/quantity[8.0 "µg-RE"]}
    {:constituent/nutrient {:nutrient/id "Na" :nutrient/parent "Minerals"}
     :measurement/quantity #broch/quantity[16.0 "mg"]}
    {:constituent/nutrient {:nutrient/id "Fiber"}
     :measurement/quantity #broch/quantity[10.0 "g"]}
    {:constituent/nutrient {:nutrient/id "Alko"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient
     {:nutrient/id "C14:0Myristinsyre" :nutrient/parent "Mettet"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient {:nutrient/id "Ca" :nutrient/parent "Minerals"}
     :measurement/quantity #broch/quantity[16.0 "mg"]}
    {:constituent/nutrient
     {:nutrient/id "C18:2n-6Linolsyre" :nutrient/parent "Flerum"}
     :measurement/quantity #broch/quantity[0.21 "g"]}
    {:constituent/nutrient
     {:nutrient/id "Vit D" :nutrient/parent "FatSolubleVitamins"}
     :measurement/quantity #broch/quantity[0.0 "µg"]}
    {:constituent/nutrient
     {:nutrient/id "Folat" :nutrient/parent "WaterSolubleVitamins"}
     :measurement/quantity #broch/quantity[0.0 "µg"]}
    {:constituent/nutrient
     {:nutrient/id "C20:4n-6Arakidonsyre" :nutrient/parent "Flerum"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient {:nutrient/id "Sukker" :nutrient/parent "Karbo"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient
     {:nutrient/id "C20:3n-6DihomoGammaLinolensyre" :nutrient/parent "Flerum"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient {:nutrient/id "Stivel" :nutrient/parent "Karbo"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient {:nutrient/id "Enumet" :nutrient/parent "Fett"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient
     {:nutrient/id "Vit B2" :nutrient/parent "WaterSolubleVitamins"}
     :measurement/quantity #broch/quantity[0.16 "mg"]}
    {:constituent/nutrient
     {:nutrient/id "Vit B1" :nutrient/parent "WaterSolubleVitamins"}
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient {:nutrient/id "Mg" :nutrient/parent "Minerals"}
     :measurement/quantity #broch/quantity[16.0 "mg"]}
    {:constituent/nutrient {:nutrient/id "Retinol" :nutrient/parent "Vit A"}
     :measurement/quantity #broch/quantity[0.0 "µg"]}
    {:constituent/nutrient
     {:nutrient/id "C22:5n-3Dokosapentaensyre" :nutrient/parent "Flerum"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient {:nutrient/id "Cu" :nutrient/parent "TraceElements"}
     :measurement/quantity #broch/quantity[0.11 "mg"]}
    {:constituent/nutrient {:nutrient/id "Mettet" :nutrient/parent "Fett"}
     :measurement/quantity #broch/quantity[0.1 "g"]}
    {:constituent/nutrient
     {:nutrient/id "Vit E" :nutrient/parent "FatSolubleVitamins"}
     :measurement/quantity #broch/quantity[1.5 "mg-ATE"]}
    {:constituent/nutrient {:nutrient/id "B-karo" :nutrient/parent "Vit A"}
     :measurement/quantity #broch/quantity[91.0 "µg"]}
    {:constituent/nutrient {:nutrient/id "P" :nutrient/parent "Minerals"}
     :measurement/quantity #broch/quantity[43.0 "mg"]}
    {:constituent/nutrient
     {:nutrient/id "Vit B12" :nutrient/parent "WaterSolubleVitamins"}
     :measurement/quantity #broch/quantity[0.0 "µg"]}
    {:constituent/nutrient
     {:nutrient/id "Niacin" :nutrient/parent "WaterSolubleVitamins"}
     :measurement/quantity #broch/quantity[0.9 "mg"]}
    {:constituent/nutrient {:nutrient/id "K" :nutrient/parent "Minerals"}
     :measurement/quantity #broch/quantity[540.0 "mg"]}
    {:constituent/nutrient {:nutrient/id "Trans" :nutrient/parent "Fett"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient {:nutrient/id "C16:1" :nutrient/parent "Enumet"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient {:nutrient/id "Fett"}
     :measurement/quantity #broch/quantity[0.5 "g"]}
    {:constituent/nutrient
     {:nutrient/id "C20:4n-3Eikosatetraensyre" :nutrient/parent "Flerum"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient {:nutrient/id "Fe" :nutrient/parent "TraceElements"}
     :measurement/quantity #broch/quantity[0.5 "mg"]}
    {:constituent/nutrient {:nutrient/id "Omega-6" :nutrient/parent "Fett"}
     :measurement/quantity #broch/quantity[0.21 "g"]}
    {:constituent/nutrient {:nutrient/id "Omega-3" :nutrient/parent "Fett"}
     :measurement/quantity #broch/quantity[0.04 "g"]}
    {:constituent/nutrient {:nutrient/id "Zn" :nutrient/parent "TraceElements"}
     :measurement/quantity #broch/quantity[0.5 "mg"]}
    {:constituent/nutrient {:nutrient/id "Flerum" :nutrient/parent "Fett"}
     :measurement/quantity #broch/quantity[0.3 "g"]}
    {:constituent/nutrient
     {:nutrient/id "C18:0Stearinsyre" :nutrient/parent "Mettet"}
     :measurement/quantity #broch/quantity[0.02 "g"]}
    {:constituent/nutrient {:nutrient/id "C18:1" :nutrient/parent "Enumet"}
     :measurement/quantity #broch/quantity[0.03 "g"]}
    {:constituent/nutrient {:nutrient/id "Mono+Di" :nutrient/parent "Karbo"}
     :measurement/quantity #broch/quantity[60.1 "g"]}
    {:constituent/nutrient
     {:nutrient/id "C20:3n-3Eikosatriensyre" :nutrient/parent "Flerum"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient {:nutrient/id "Protein"}
     :measurement/quantity #broch/quantity[2.0 "g"]}
    {:constituent/nutrient {:nutrient/id "Se" :nutrient/parent "TraceElements"}
     :measurement/quantity #broch/quantity[0.0 "µg"]}
    {:constituent/nutrient
     {:nutrient/id "C16:0Palmitinsyre" :nutrient/parent "Mettet"}
     :measurement/quantity #broch/quantity[0.12 "g"]}
    {:constituent/nutrient
     {:nutrient/id "Vit C" :nutrient/parent "WaterSolubleVitamins"}
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient
     {:nutrient/id "C20:5n-3Eikosapentaensyre" :nutrient/parent "Flerum"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient
     {:nutrient/id "C22:6n-3Dokosaheksaensyre" :nutrient/parent "Flerum"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient {:nutrient/id "Vann"}
     :measurement/quantity #broch/quantity[28.0 "g"]}
    {:constituent/nutrient {:nutrient/id "NaCl" :nutrient/parent "Minerals"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient {:nutrient/id "I" :nutrient/parent "TraceElements"}
     :measurement/quantity #broch/quantity[8.0 "µg"]}
    {:constituent/nutrient
     {:nutrient/id "C12:0Laurinsyre" :nutrient/parent "Mettet"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient {:nutrient/id "Karbo"}
     :measurement/quantity #broch/quantity[60.1 "g"]}]})

(def ghee
  {:food/id "08.252"
   :food/calories {:measurement/observation "879"}
   :food/name {:nb "Ghee"}
   :food/energy {:measurement/quantity #broch/quantity[3612.9 "kJ"]}
   :food/constituents
   [{:constituent/nutrient {:nutrient/id "Fett"}
     :measurement/quantity #broch/quantity[97.6 "g"]}
    {:constituent/nutrient {:nutrient/id "Fe" :nutrient/parent "TraceElements"}
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient {:nutrient/id "C18:1" :nutrient/parent "Enumet"}
     :measurement/quantity nil}
    {:constituent/nutrient
     {:nutrient/id "C18:2n-6Linolsyre" :nutrient/parent "Flerum"}
     :measurement/quantity nil}
    {:constituent/nutrient
     {:nutrient/id "C16:0Palmitinsyre" :nutrient/parent "Mettet"}
     :measurement/quantity nil}
    {:constituent/nutrient {:nutrient/id "Enumet" :nutrient/parent "Fett"}
     :measurement/quantity #broch/quantity[25.7 "g"]}
    {:constituent/nutrient {:nutrient/id "Retinol" :nutrient/parent "Vit A"}
     :measurement/quantity #broch/quantity[922.0 "µg"]}
    {:constituent/nutrient {:nutrient/id "Alko"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient
     {:nutrient/id "Vit D" :nutrient/parent "FatSolubleVitamins"}
     :measurement/quantity #broch/quantity[1.1 "µg"]}
    {:constituent/nutrient {:nutrient/id "Vann"}
     :measurement/quantity #broch/quantity[2.0 "g"]}
    {:constituent/nutrient {:nutrient/id "Zn" :nutrient/parent "TraceElements"}
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient {:nutrient/id "Mg" :nutrient/parent "Minerals"}
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient {:nutrient/id "Kolest" :nutrient/parent "Fett"}
     :measurement/quantity #broch/quantity[246.0 "mg"]}
    {:constituent/nutrient
     {:nutrient/id "Vit B12" :nutrient/parent "WaterSolubleVitamins"}
     :measurement/quantity #broch/quantity[0.0 "µg"]}
    {:constituent/nutrient {:nutrient/id "Omega-6" :nutrient/parent "Fett"}
     :measurement/quantity nil}
    {:constituent/nutrient
     {:nutrient/id "C18:3n-3AlfaLinolensyre" :nutrient/parent "Flerum"}
     :measurement/quantity nil}
    {:constituent/nutrient
     {:nutrient/id "C18:0Stearinsyre" :nutrient/parent "Mettet"}
     :measurement/quantity nil}
    {:constituent/nutrient {:nutrient/id "Flerum" :nutrient/parent "Fett"}
     :measurement/quantity #broch/quantity[4.6 "g"]}
    {:constituent/nutrient
     {:nutrient/id "Folat" :nutrient/parent "WaterSolubleVitamins"}
     :measurement/quantity #broch/quantity[0.0 "µg"]}
    {:constituent/nutrient
     {:nutrient/id "C12:0Laurinsyre" :nutrient/parent "Mettet"}
     :measurement/quantity nil}
    {:constituent/nutrient
     {:nutrient/id "C22:6n-3Dokosaheksaensyre" :nutrient/parent "Flerum"}
     :measurement/quantity nil}
    {:constituent/nutrient
     {:nutrient/id "C20:5n-3Eikosapentaensyre" :nutrient/parent "Flerum"}
     :measurement/quantity nil}
    {:constituent/nutrient
     {:nutrient/id "C14:0Myristinsyre" :nutrient/parent "Mettet"}
     :measurement/quantity nil}
    {:constituent/nutrient {:nutrient/id "P" :nutrient/parent "Minerals"}
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient {:nutrient/id "Protein"}
     :measurement/quantity #broch/quantity[0.1 "g"]}
    {:constituent/nutrient {:nutrient/id "NaCl" :nutrient/parent "Minerals"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient
     {:nutrient/id "C22:5n-3Dokosapentaensyre" :nutrient/parent "Flerum"}
     :measurement/quantity nil}
    {:constituent/nutrient
     {:nutrient/id "C20:3n-3Eikosatriensyre" :nutrient/parent "Flerum"}
     :measurement/quantity nil}
    {:constituent/nutrient {:nutrient/id "Se" :nutrient/parent "TraceElements"}
     :measurement/quantity #broch/quantity[0.0 "µg"]}
    {:constituent/nutrient
     {:nutrient/id "Vit B6" :nutrient/parent "WaterSolubleVitamins"}
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient
     {:nutrient/id "Vit E" :nutrient/parent "FatSolubleVitamins"}
     :measurement/quantity #broch/quantity[5.8 "mg-ATE"]}
    {:constituent/nutrient {:nutrient/id "I" :nutrient/parent "TraceElements"}
     :measurement/quantity #broch/quantity[44.0 "µg"]}
    {:constituent/nutrient {:nutrient/id "B-karo" :nutrient/parent "Vit A"}
     :measurement/quantity #broch/quantity[1860.0 "µg"]}
    {:constituent/nutrient
     {:nutrient/id "Vit C" :nutrient/parent "WaterSolubleVitamins"}
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient {:nutrient/id "Sukker" :nutrient/parent "Karbo"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient
     {:nutrient/id "Niacin" :nutrient/parent "WaterSolubleVitamins"}
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient {:nutrient/id "Omega-3" :nutrient/parent "Fett"}
     :measurement/quantity nil}
    {:constituent/nutrient
     {:nutrient/id "Vit A" :nutrient/parent "FatSolubleVitamins"}
     :measurement/quantity #broch/quantity[1077.0 "µg-RE"]}
    {:constituent/nutrient {:nutrient/id "Na" :nutrient/parent "Minerals"}
     :measurement/quantity #broch/quantity[1.0 "mg"]}
    {:constituent/nutrient
     {:nutrient/id "Vit B1" :nutrient/parent "WaterSolubleVitamins"}
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient
     {:nutrient/id "C20:4n-6Arakidonsyre" :nutrient/parent "Flerum"}
     :measurement/quantity nil}
    {:constituent/nutrient {:nutrient/id "Ca" :nutrient/parent "Minerals"}
     :measurement/quantity #broch/quantity[1.0 "mg"]}
    {:constituent/nutrient {:nutrient/id "K" :nutrient/parent "Minerals"}
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient {:nutrient/id "Trans" :nutrient/parent "Fett"}
     :measurement/quantity #broch/quantity[2.1 "g"]}
    {:constituent/nutrient {:nutrient/id "Cu" :nutrient/parent "TraceElements"}
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient
     {:nutrient/id "C20:3n-6DihomoGammaLinolensyre" :nutrient/parent "Flerum"}
     :measurement/quantity nil}
    {:constituent/nutrient {:nutrient/id "Fiber"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient {:nutrient/id "Stivel" :nutrient/parent "Karbo"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient {:nutrient/id "C16:1" :nutrient/parent "Enumet"}
     :measurement/quantity nil}
    {:constituent/nutrient
     {:nutrient/id "Vit B2" :nutrient/parent "WaterSolubleVitamins"}
     :measurement/quantity #broch/quantity[0.0 "mg"]}
    {:constituent/nutrient
     {:nutrient/id "C20:4n-3Eikosatetraensyre" :nutrient/parent "Flerum"}
     :measurement/quantity nil}
    {:constituent/nutrient {:nutrient/id "Mono+Di" :nutrient/parent "Karbo"}
     :measurement/quantity #broch/quantity[0.0 "g"]}
    {:constituent/nutrient {:nutrient/id "Mettet" :nutrient/parent "Fett"}
     :measurement/quantity #broch/quantity[58.4 "g"]}
    {:constituent/nutrient {:nutrient/id "Karbo"}
     :measurement/quantity #broch/quantity[0.0 "g"]}]})

(deftest diff-foods-test
  (testing "Diffs main nutrition of two foods"
    (is (= (->> (sut/diff-constituents
                 medians
                 (sut/food->diffable dried-apple)
                 (sut/food->diffable ghee)))
           [{:id "08.252"
             :diffs {"Fett" 4.9414758269720105
                     "Karbo" -1.6287262872628727
                     "Fiber" -0.6451612903225806
                     "Vann" -0.52
                     "Alko" 0.0
                     "Protein" -0.12709030100334448}}]))))

(deftest rate-energy-diff
  (testing "3x the energy is a dramatic diff"
    (is (= (sut/rate-energy-diff
            ["06.531" 1151.8]
            ["08.252" 3612.9])
           [{:id "08.252"
             :diff 0.3188020703589914
             :rating ::sut/dramatic}])))

  (testing "1/3 of the energy is a dramatic diff"
    (is (= (sut/rate-energy-diff
            ["08.252" 3612.9]
            ["06.531" 1151.8])
           [{:id "06.531"
             :diff 3.136742490015628
             :rating ::sut/dramatic}])))

  (testing "noticeable diff"
    (is (= (sut/rate-energy-diff
            ["06.531" 1151.8]
            ["08.252" 1612.5])
           [{:id "08.252"
             :diff 0.7142945736434109
             :rating ::sut/noticeable}])))

  (testing "small diff"
    (is (= (sut/rate-energy-diff
            ["06.531" 200]
            ["08.252" 240])
           [{:id "08.252"
             :diff 5/6
             :rating ::sut/small}])))

  (testing "slight diff"
    (is (= (sut/rate-energy-diff
            ["06.531" 200]
            ["08.252" 218])
           [{:id "08.252"
             :diff 100/109
             :rating ::sut/slight}]))))

(deftest energy-equivalents-test
  (testing "Finds the required amount to get the same amount of energy"
    (is (= (sut/get-energy-equivalents
            ["06.531" 200]
            ["08.252" 250])
           [{:id "08.252"
             :amount 0.8}]))))
