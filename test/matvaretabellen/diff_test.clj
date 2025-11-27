(ns matvaretabellen.diff-test
  (:require [clojure.test :refer [deftest is testing]]
            [matvaretabellen.diff :as sut]
            [matvaretabellen.food :as food]))

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
     :measurement/quantity #broch/quantity[8.0 "RAE"]}
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
     :measurement/quantity #broch/quantity[1077.0 "RAE"]}
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
                 [(food/food->diffable dried-apple)
                  (food/food->diffable ghee)]))
           [{:id "08.252"
             :diffs {"Fett" 4.9414758269720105
                     "Karbo" -1.6287262872628727
                     "Fiber" -0.6451612903225806
                     "Vann" -0.52
                     "Alko" 0.0
                     "Protein" -0.12709030100334448}}]))))

(deftest find-notable-diffs-test
  (testing "Returns a map of the nutrients with the biggest diff"
    (is (= (sut/find-notable-diffs
            0.5
            [{:id "04.299"
              :diffs
              {"Alko" 0
               "Cu" -0.031496062992125984
               "Vit B6" 0.12727272727272726
               "NaCl" 0.023809523809523808
               "C16:0Palmitinsyre" 0.1134020618556701
               "I" 0.736111111111111
               "Vit B1" 0.018348623853211007
               "C20:3n-3Eikosatriensyre" 0
               "Folat" 0.0625
               "C20:5n-3Eikosapentaensyre" 0.047058823529411764
               "C22:6n-3Dokosaheksaensyre" 0.13559322033898305
               "C20:4n-6Arakidonsyre" 0.05555555555555556
               "Niacin" 0.10256410256410256
               "Karbo" -0.18970189701897022
               "Kolest" 0.3448275862068966
               "Mg" 0.06315789473684211
               "Omega-3" 0.10108303249097475
               "Protein" 0.3210702341137124
               "Enumet" 0.05084745762711864
               "Vit D" 0.06557377049180328
               "C18:3n-3AlfaLinolensyre" 0.04210526315789474
               "P" 0.3425925925925926
               "Ca" 0.12244897959183673
               "Fett" 0.09669211195928754
               "C18:2n-6Linolsyre" 0.07883817427385892
               "Se" 0.25
               "Vann" 0.040000000000000036
               "Zn" 0.07692307692307693
               "Mono+Di" -0.34403669724770647
               "Trans" 0
               "Sukker" 0
               "C20:4n-3Eikosatetraensyre" 0
               "Mettet" 0.06306306306306306
               "Omega-6" 0.08823529411764706
               "Na" 0.09496402877697842
               "Vit C" 0.017543859649122806
               "Fiber" -0.16129032258064516
               "Vit A" 0.33802816901408456
               "Stivel" 0.02443280977312391
               "Fe" 0.014814814814814815
               "C20:3n-6DihomoGammaLinolensyre" 0
               "C18:1" 0.11410788381742738
               "C12:0Laurinsyre" 0.039408866995073885
               "Vit B12" 0.18867924528301888
               "C18:0Stearinsyre" 0.07349081364829398
               "C14:0Myristinsyre" 0.06060606060606061
               "K" 0.22611464968152867
               "C22:5n-3Dokosapentaensyre" 0.05555555555555556
               "Flerum" 0.050955414012738856
               "Vit E" 0.03592814371257484
               "Retinol" 0.12337662337662338
               "Vit B2" 0.10344827586206896
               "C16:1" 0.048
               "B-karo" 2.003992015968064}}
             {:id "08.252"
              :diffs
              {"Alko" 0
               "Cu" -0.07874015748031496
               "Vit B6" -0.07272727272727272
               "NaCl" 0
               "C16:0Palmitinsyre" 0
               "I" 0.5833333333333334
               "Vit B1" -0.018348623853211007
               "C20:3n-3Eikosatriensyre" 0
               "Folat" -0.0375
               "C20:5n-3Eikosapentaensyre" 0
               "C22:6n-3Dokosaheksaensyre" 0
               "C20:4n-6Arakidonsyre" 0
               "Niacin" -0.017094017094017096
               "Karbo" -0.23848238482384826
               "Kolest" 2.8275862068965516
               "Mg" -0.042105263157894736
               "Omega-3" 0
               "Protein" 0.006688963210702342
               "Enumet" 2.1779661016949152
               "Vit D" 0.18032786885245905
               "C18:3n-3AlfaLinolensyre" 0
               "P" -0.037037037037037035
               "Ca" -0.02040816326530612
               "Fett" 4.961832061068702
               "C18:2n-6Linolsyre" 0
               "Se" 0
               "Vann" -1.74
               "Zn" 0
               "Mono+Di" -0.4036697247706423
               "Trans" 2.1
               "Sukker" 0
               "C20:4n-3Eikosatetraensyre" 0
               "Mettet" 5.261261261261262
               "Omega-6" 0
               "Na" 0.0028776978417266188
               "Vit C" 0
               "Fiber" -0.1935483870967742
               "Vit A" 6.050704225352113
               "Stivel" 0
               "Fe" -0.014814814814814815
               "C20:3n-6DihomoGammaLinolensyre" 0
               "C18:1" 0
               "C12:0Laurinsyre" 0
               "Vit B12" 0
               "C18:0Stearinsyre" 0
               "C14:0Myristinsyre" 0
               "K" -0.19745222929936307
               "C22:5n-3Dokosapentaensyre" 0
               "Flerum" 0.5859872611464968
               "Vit E" 0.6586826347305388
               "Retinol" 5.987012987012987
               "Vit B2" 0
               "C16:1" 0
               "B-karo" 7.289421157684631}}])
           {"B-karo" 7.289421157684631
            "Enumet" 2.1779661016949152
            "Fett" 4.961832061068702
            "Flerum" 0.5859872611464968
            "I" 0.736111111111111
            "Kolest" 2.8275862068965516
            "Mettet" 5.261261261261262
            "Retinol" 5.987012987012987
            "Trans" 2.1
            "Vann" -1.74
            "Vit A" 6.050704225352113
            "Vit E" 0.6586826347305388}))))

(deftest rate-energy-diff
  (testing "3x the energy is a dramatic diff"
    (is (= (sut/rate-energy-diff
            [["06.531" 1151.8]
             ["08.252" 3612.9]])
           [{:id "08.252"
             :diff 0.3188020703589914
             :rating ::sut/dramatic}])))

  (testing "1/3 of the energy is a dramatic diff"
    (is (= (sut/rate-energy-diff
            [["08.252" 3612.9]
             ["06.531" 1151.8]])
           [{:id "06.531"
             :diff 3.136742490015628
             :rating ::sut/dramatic}])))

  (testing "noticeable diff"
    (is (= (sut/rate-energy-diff
            [["06.531" 1151.8]
             ["08.252" 1612.5]])
           [{:id "08.252"
             :diff 0.7142945736434109
             :rating ::sut/moderate}])))

  (testing "small diff"
    (is (= (sut/rate-energy-diff
            [["06.531" 200]
             ["08.252" 240]])
           [{:id "08.252"
             :diff 5/6
             :rating ::sut/slight}])))

  (testing "slight diff"
    (is (= (sut/rate-energy-diff
            [["06.531" 200]
             ["08.252" 218]])
           [{:id "08.252"
             :diff 100/109
             :rating ::sut/similar}]))))

(deftest energy-equivalents-test
  (testing "Finds the required amount to get the same amount of energy"
    (is (= (sut/get-energy-equivalents
            [["06.531" 200]
             ["08.252" 250]])
           [{:id "08.252"
             :amount 0.8}]))))
