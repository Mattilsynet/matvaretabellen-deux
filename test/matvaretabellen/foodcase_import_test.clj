(ns matvaretabellen.foodcase-import-test
  (:require [clojure.test :refer [deftest is testing]]
            [matvaretabellen.foodcase-import :as sut]))

(def wheat-flakes
  {"groupId" "5.3"
   "id" "05.156"
   "name" "Wheat flakes, roasted, All-Bran Regular"
   "slug" "wheat-flakes-roasted-all-bran-regular"
   "synonym" "breakfast cereal;kellogg's;ultra processed"
   "sortName" ""
   "additionalInfo" ""
   "latinFamilyName" ""
   "latinName" "Triticum aestivum L."
   "latinAuthor" ""
   "langualCodes" "A0258 A0816 B1312"
   "Portion" {"ref" "porsjon dl", "value" "35 18"}
   "Netto" {"ref" "0", "value" "100"}
   "Energi1" {"ref" "MI0114", "value" "1418.5"}
   "Energi2" {"ref" "MI0115", "value" "336"}
   "Vann" {"ref" "MI0142", "value" "11"}
   "Fett" {"ref" "225a", "value" "1.8"}
   "Folat" {"ref" "MI0232", "value" ""}
   "Vit A" {"ref" "MI0114" "value" "23"}
   "Vit C" {"ref" "10", "value" ""}
   "Ca" {"ref" "10", "value" "M"}
   "Fe" {"ref" "MI0232", "value" ""}
   "Retinol" {"ref" "20", "value" "30"}
   "B-karo" {"ref" "20", "value" "180"}})

(def opt
  {:id->nutrient
   {"Vann" {:nutrient/unit "g"}
    "Fett" {:nutrient/unit "g"}
    "Vit A" {:nutrient/unit "µg-RE"}
    "Retinol" {:nutrient/unit "µg"}
    "B-karo" {:nutrient/unit "µg"}}

   :id->portion-kind
   {:porsjon {}
    :dl {}
    :glass {}
    :glass_stort {}
    :glass_lite {}}})

(deftest foodcase-food->food-test
  (testing "Parses synonyms"
    (is (= (-> (sut/foodcase-food->food wheat-flakes opt)
               :food/search-keywords)
           #{"kellogg's"
             "ultra processed"
             "breakfast cereal"})))

  (testing "Parses LanguaL codes"
    (is (= (-> (sut/foodcase-food->food wheat-flakes opt)
               :food/langual-codes)
           #{[:langual-code/id "B1312"]
             [:langual-code/id "A0816"]
             [:langual-code/id "A0258"]})))

  (testing "Parses energy"
    (is (= (-> (sut/foodcase-food->food wheat-flakes opt)
               :food/energy)
           {:measurement/quantity #broch/quantity[1418.5 "kJ"]
            :measurement/source [:source/id "MI0114"]})))

  (testing "Parses calories"
    (is (= (-> (sut/foodcase-food->food wheat-flakes opt)
               :food/calories)
           {:measurement/observation "336"
            :measurement/source [:source/id "MI0115"]})))

  (testing "Parses portions"
    (is (= (-> (sut/foodcase-food->food wheat-flakes opt)
               :food/portions)
           #{{:portion/kind [:portion-kind/id :dl]
              :portion/quantity #broch/quantity[18.0 "g"]}
             {:portion/kind [:portion-kind/id :porsjon]
              :portion/quantity #broch/quantity[35.0 "g"]}})))

  (testing "Parses tricky portion"
    (is (= (-> wheat-flakes
               (assoc "Portion" {"ref" "glass glass (stort) glass (lite) dl"
                                 "value" "200 320 150 100"})
               (sut/foodcase-food->food opt)
               :food/portions)
           #{{:portion/kind [:portion-kind/id :glass]
              :portion/quantity #broch/quantity[200.0 "g"]}
             {:portion/kind [:portion-kind/id :glass_lite]
              :portion/quantity #broch/quantity[150.0 "g"]}
             {:portion/kind [:portion-kind/id :glass_stort]
              :portion/quantity #broch/quantity[320.0 "g"]}
             {:portion/kind [:portion-kind/id :dl]
              :portion/quantity #broch/quantity[100.0 "g"]}})))

  (testing "Parses tricky portion kind when building index"
    (is (= (sut/foodcase-portiontype->portion-kind
            {"id" "glass (stort)"
             "name" "Glass (stort)"
             "unit" ""})
           {:portion-kind/id :glass_stort
            :portion-kind/name {:nb "glass (stort)", :en "glass (large)"}
            :portion-kind/unit ""})))

  (testing "Parses constituents"
    (is (= (->> (sut/foodcase-food->food wheat-flakes opt)
                :food/constituents)
           #{{:constituent/nutrient [:nutrient/id "Fett"]
              :measurement/quantity #broch/quantity[1.8 "g"]
              :measurement/source [:source/id "225a"]}
             {:constituent/nutrient [:nutrient/id "Vann"]
              :measurement/quantity #broch/quantity[11.0 "g"]
              :measurement/source [:source/id "MI0142"]}
             {:constituent/nutrient [:nutrient/id "Vit A RE"]
              :measurement/source [:source/id "MI0322"]
              :measurement/quantity #broch/quantity[60 "µg-RE"]}
             {:constituent/nutrient [:nutrient/id "Vit A"]
              :measurement/source [:source/id "MI0114"]
              :measurement/quantity #broch/quantity[23.0 "µg-RE"]}
             {:constituent/nutrient [:nutrient/id "Retinol"]
              :measurement/source [:source/id "20"]
              :measurement/quantity #broch/quantity[30.0 "µg"]}
             {:constituent/nutrient [:nutrient/id "B-karo"]
              :measurement/source [:source/id "20"]
              :measurement/quantity #broch/quantity[180.0 "µg"]}}))))

(deftest find-key-paths--test
  (testing "finds interesting keys in nested data"
    (is (= (sut/find-key-paths
            {:i18n/title "Title"
             :flavor "bland"
             :data [{:i18n/title "Item title"
                     :i18n/description "Bland tasting"
                     :styles {:color "beige"}}]}
            #{:i18n/title :i18n/description})
           [[:i18n/title]
            [:data 0 :i18n/title]
            [:data 0 :i18n/description]]))))

(deftest combine-i18n-sources
  (testing "Combines multiple translations into one i18n-ed data structure"
    (is (= (sut/combine-i18n-sources
            {:en [{:i18n/title "Title"
                   :flavor "bland"
                   :data [{:i18n/title "Item title"
                           :i18n/description [:div "Bland tasting"]
                           :styles {:color "beige"}}]}]
             :nb [{:i18n/title "Tittel"
                   :flavor "bland"
                   :data '({:i18n/title "Greietittel"
                            :i18n/description [:span "Kjedelig smak"]
                            :styles {:color "beige"}})}]}
            #{:i18n/title :i18n/description})
           [{:i18n/title {:en "Title"
                          :nb "Tittel"}
             :flavor "bland"
             :data [{:i18n/title {:en "Item title"
                                  :nb "Greietittel"}
                     :i18n/description {:en [:div "Bland tasting"]
                                        :nb [:span "Kjedelig smak"]}
                     :styles {:color "beige"}}]}]))))

(deftest validate-i18n-combination
  (testing "All localized food lists must be equal sans i18n attributes"
    (is (nil? (sut/validate-i18n-combination
               {:en [{:i18n/title "Title"
                      :flavor "bland"
                      :data [{:i18n/title "Item title"
                              :i18n/description [:div "Bland tasting"]
                              :styles {:color "beige"}}]}]
                :nb [{:i18n/title "Tittel"
                      :flavor "bland"
                      :data '({:i18n/title "Greietittel"
                               :i18n/description [:span "Kjedelig smak"]
                               :styles {:color "beige"}})}]}
               #{:i18n/title :i18n/description}))))

  (testing "Localized food lists cannot differ in non-i18n attributes"
    (is (= (sut/validate-i18n-combination
            {:en [{:i18n/title "Title"
                   :flavor "bland"}]
             :nb [{:i18n/title "Tittel"
                   :flavor "kjedelig"}]}
            #{:i18n/title :i18n/description})
           {:explanation "Localized food sources are not all alike"
            :data {:ms [[{:flavor "bland"}]
                        [{:flavor "kjedelig"}]]}})))

  (testing "Can't use i18n attribute inside a set"
    (is (= (sut/validate-i18n-combination
            {:en #{{:i18n/title "Title"
                    :flavor "bland"}}
             :nb #{{:i18n/title "Tittel"
                    :flavor "bland"}}}
            #{:i18n/title :i18n/description})
           {:explanation "Can't use i18n attributes inside a set"
            :data {}}))))

(deftest foodcase-foodgroup->food-group
  (is (= (sut/foodcase-foodgroup->food-group
          {"id" "1"
           "parentId" ""
           "name" "Melk og melkeprodukter"})
         {:food-group/id "1"
          :food-group/name "Melk og melkeprodukter"}))

  (is (= (sut/foodcase-foodgroup->food-group
          {"id" "1.1"
           "parentId" "1"
           "name" "Melk og melkebasert drikke"})
         {:food-group/id "1.1"
          :food-group/name "Melk og melkebasert drikke"
          :food-group/parent {:food-group/id "1"}}))

  (is (= (sut/foodcase-foodgroup->food-group
          {"id" "1.4.1"
           "parentId" "1.4"
           "name" "Ost, ekstra fet - UT"})
         nil)))

(deftest foodcase-nutrient->nutrient
  (testing "known non-constituents are not included with nutrients"
    (is (nil? (sut/foodcase-nutrient->nutrient
               {"id" "Netto"}))))

  (is (= (sut/foodcase-nutrient->nutrient
          {"parentId" ""
           "structuralLevel" ""
           "id" "Vann"
           "name" "Vann"
           "shortName" "Vann"
           "euroFIR" "WATER"
           "euroFIRname" "water"
           "unit" "g"
           "decimals" "1"})
         {:nutrient/id "Vann"
          :nutrient/name "Vann"
          :nutrient/euro-fir-id "WATER"
          :nutrient/euro-fir-name "water"
          :nutrient/unit "g"
          :nutrient/decimal-precision 0 ;; ignored from FoodCASE, uses our
          }))

  (testing "Includes parentId when non-empty"
    (is (= (:nutrient/parent (sut/foodcase-nutrient->nutrient
                              {"parentId" "Vit A"
                               "unit" "g"}))
           {:nutrient/id "Vit A"}))))
