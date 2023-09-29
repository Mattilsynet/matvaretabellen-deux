(ns matvaretabellen.ingest-test
  (:require [matvaretabellen.ingest :as sut]
            [clojure.test :refer [deftest is testing]]))

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
   "Vit C" {"ref" "10", "value" ""}
   "Ca" {"ref" "10", "value" "M"}
   "Fe" {"ref" "MI0232", "value" ""}})

(deftest foodcase-food->food-test
  (testing "Parses synonyms"
    (is (= (-> (sut/foodcase-food->food wheat-flakes)
               :food/search-keywords)
           #{"kellogg's"
             "ultra processed"
             "breakfast cereal"})))

  (testing "Parses LanguaL codes"
    (is (= (-> (sut/foodcase-food->food wheat-flakes)
               :food/langual-codes)
           #{[:langual-code/id "B1312"]
             [:langual-code/id "A0816"]
             [:langual-code/id "A0258"]})))

  (testing "Parses energy"
    (is (= (-> (sut/foodcase-food->food wheat-flakes)
               :food/energy)
           {:measurement/quantity #broch/quantity[1418.5 "J"]
            :measurement/source [:origin/id "MI0114"]})))

  (testing "Parses calories"
    (is (= (-> (sut/foodcase-food->food wheat-flakes)
               :food/calories)
           {:measurement/observation "336"
            :measurement/source [:origin/id "MI0115"]})))

  (testing "Parses portions"
    (is (= (-> (sut/foodcase-food->food wheat-flakes)
               :food/portions)
           #{{:portion/kind [:portion-kind/id :dl]
              :portion/quantity #broch/quantity[18.0 "g"]}
             {:portion/kind [:portion-kind/id :porsjon]
              :portion/quantity #broch/quantity[35.0 "g"]}})))

  (testing "Parses constituents"
    (is (= (->> (sut/foodcase-food->food wheat-flakes)
                :food/constituents)
           #{{:constituent/nutrient [:nutrient/id "Fett"]
              :measurement/quantity #broch/quantity[1.8 "g"]
              :measurement/source [:source/id "225a"]}
             {:constituent/nutrient [:nutrient/id "Vann"]
              :measurement/quantity #broch/quantity[11.0 "g"]
              :measurement/source [:source/id "MI0142"]}

             ;; constituents without a known quantity still needs to be
             ;; represented due to differing reasons for said void.
             {:constituent/nutrient [:nutrient/id "Folat"]
              :measurement/source [:source/id "MI0232"]}
             {:constituent/nutrient [:nutrient/id "Fe"]
              :measurement/source [:source/id "MI0232"]}
             {:constituent/nutrient [:nutrient/id "Ca"]
              :measurement/source [:source/id "10"]}
             {:constituent/nutrient [:nutrient/id "Vit C"]
              :measurement/source [:source/id "10"]}})))
  )

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

(deftest validate-food-sources
  (testing "All localized food lists must be equal sans i18n attributes"
    (is (nil? (sut/validate-food-sources
               #{:i18n/title :i18n/description}
               {:en [{:i18n/title "Title"
                      :flavor "bland"
                      :data [{:i18n/title "Item title"
                              :i18n/description [:div "Bland tasting"]
                              :styles {:color "beige"}}]}]
                :nb [{:i18n/title "Tittel"
                      :flavor "bland"
                      :data '({:i18n/title "Greietittel"
                               :i18n/description [:span "Kjedelig smak"]
                               :styles {:color "beige"}})}]}))))

  (testing "Localized food lists cannot differ in non-i18n attributes"
    (is (= (sut/validate-food-sources
            #{:i18n/title :i18n/description}
            {:en [{:i18n/title "Title"
                   :flavor "bland"}]
             :nb [{:i18n/title "Tittel"
                   :flavor "kjedelig"}]})
           {:explanation "Localized food sources are not all alike"})))

  (testing "Can't use i18n attribute inside a set"
    (is (= (sut/validate-food-sources
            #{:i18n/title :i18n/description}
            {:en #{{:i18n/title "Title"
                    :flavor "bland"}}
             :nb #{{:i18n/title "Tittel"
                    :flavor "bland"}}})
           {:explanation "Can't use i18n attributes inside a set"}))))
