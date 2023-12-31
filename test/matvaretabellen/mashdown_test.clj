(ns matvaretabellen.mashdown-test
  (:require [clojure.test :refer [deftest is testing]]
            [matvaretabellen.faux-food-db :refer [with-test-db]]
            [matvaretabellen.mashdown :as sut]))

(deftest render
  (testing "Samme tekst som navnet på næringsstoffet"
    (is (= (with-test-db [db [{:nutrient/id "Protein"
                               :nutrient/name {:nb "protein"}}]]
             (sut/render db :nb "Her er det [protein], må vite!"))
           '("Her er det "
             [:a {:href "/protein/"} "protein"]
             ", må vite!"))))

  (testing "Annen tekst enn på næringsstoffet"
    (is (= (with-test-db [db [{:nutrient/id "Karbo"
                               :nutrient/name {:nb "karbohydrat"
                                               :en "carbohydrate"}}]]
             [(sut/render db :nb "Her er det [kærbs|Karbo], må vite!")
              (sut/render db :en "Here's the [carbos|Karbo], fo sho!")])
           ['("Her er det " [:a {:href "/karbohydrat/"} "kærbs"] ", må vite!")
            '("Here's the " [:a {:href "/en/carbohydrate/"} "carbos"] ", fo sho!")])))

  (testing "Matvare-ID"
    (is (= (with-test-db [db [{:food/id "05.448"
                               :food/name {:nb "Banankake"}}]]
             (sut/render db :nb "Klar for litt [kake med banan|05.448]?"))
           '("Klar for litt " [:a {:href "/banankake/"} "kake med banan"] "?"))))

  (testing "Matvaregruppe"
    (is (= (with-test-db [db [{:food-group/id "1"
                               :food-group/name {:nb "Melk og melkeprodukter"}}]]
             (sut/render db :nb "Det er fett i [melkeprodukter|fg-1]!"))
           '("Det er fett i " [:a {:href "/gruppe/melk-og-melkeprodukter/"} "melkeprodukter"] "!"))))

  (testing "Relativ URL"
    (is (= (with-test-db [db []]
             (sut/render db :nb "En lenke til søket på [forsiden|/?search=ris] hvis du vil ha ris."))
           '("En lenke til søket på " [:a {:href "/?search=ris"} "forsiden"] " hvis du vil ha ris.")))))

(deftest strip
  (is (= (sut/strip "Her er det [protein], må vite!")
         "Her er det protein, må vite!"))

  (is (= (sut/strip "Klar for litt [kake med banan|05.448]?")
         "Klar for litt kake med banan?")))
