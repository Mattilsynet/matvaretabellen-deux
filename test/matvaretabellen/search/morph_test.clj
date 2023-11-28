(ns matvaretabellen.search.morph-test
  (:require [clojure.test :refer [deftest is testing]]
            [matvaretabellen.search.morph :as sut]))

(deftest split-compound-words-test
  (testing "Splits compound words into their parts"
    (is (= (sut/split-compound-word "Samferdselsminister")
           ["samferdsel" "minister"]))

    (is (= (sut/split-compound-word "makrellstørje")
           ["makrell" "størje"]))))
