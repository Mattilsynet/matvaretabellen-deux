(ns matvaretabellen.food-name-test
  (:require [clojure.test :refer [deftest is testing]]
            [matvaretabellen.food-name :as sut]))

(deftest shorten-name-test
  (testing "Shortens food name"
    (is (= (sut/shorten-name "Barnemat, havregrøt med banan og mango, fra 8 mnd, pulver, Nestlé")
           "Barnemat, havregrøt")))

  (testing "Shortens some common words, if possible"
    (is (= (sut/shorten-name "Havregrøt med eple og drue, fra 8 mnd, pulver, Nestlé")
           "Havregrøt eple/drue")))

  (testing "Abbreviates words if necessary"
    (is (= (sut/shorten-name "Barnemat, fullkornspasta med grønnsaker, fra 12 mnd"
                             {:abbreviate-words? true})
           "Barnemat, fullkor...")))

  (testing "Does not abbreviate unnecessary"
    (is (= (-> "Barnemat, havregrøt med banan og mango, fra 8 mnd, pulver, Nestlé"
               (sut/shorten-name {:abbreviate-words? true}))
           "Barnemat, havregrøt"))

    (is (= (-> "Barnemat, middag, potet og brokkoli, fra 4 mnd"
               (sut/shorten-name {:abbreviate-words? true}))
           "Barnemat, middag")))

  (testing "Does not abbreviate numbers"
    (is (= (-> "Vitamineral, 0,55 g/stk, Vitaplex"
               sut/shorten-name)
           "Vitamineral"))))

(deftest shorten-names-test
  (testing "Shortens food names"
    (is (= (sut/shorten-names
            ["Barnemat, havregrøt med banan og mango, fra 8 mnd, pulver, Nestlé"
             "Barnemat, middag, fullkornspasta med grønnsaker, fra 12 mnd"
             "Barnemat, middag, couscous og biff, fra 12 mnd"
             "Barnemat, middag, oksekjøtt og potet, fra 12 mnd"])
           ["Barnemat, havregrøt"
            "Barnemat, fullkor..."
            "Barnemat, couscou..."
            "Barnemat, oksekjø..."]))))
