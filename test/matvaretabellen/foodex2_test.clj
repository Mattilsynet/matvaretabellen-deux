(ns matvaretabellen.foodex2-test
  (:require [clojure.test :refer [deftest is]]
            [matvaretabellen.foodex2 :as foodex2]))

(deftest render-aspect
  (is (= (foodex2/render-aspect {:foodex2/term
                                 {:foodex2.term/code "A01LC"
                                  :foodex2.term/name "Common banana"
                                  :foodex2.term/note "The group includes any type of common bananas or dessert bananas, which are high in sugar and usually eaten without cooking, as fruit. They include all the dessert cultivars of Musa acuminata non-hybrid groups (Musa AA, AAA, and AAAA Groups) and also hybrids (Musa x paradisiaca AB, AAB, ABB, ABBB, AAAB, AABB Groups). The part consumed/analysed is not specified. When relevant, information on the part consumed/analysed has to be reported with additional facet descriptors. In case of data collections related to legislations, the default part consumed/analysed is the one defined in the applicable legislation."}})
         [:abbr {:title "The group includes any type of common bananas or dessert bananas, which are high in sugar and usually eaten without cooking, as fruit. They include all the dessert cultivars of Musa acuminata non-hybrid groups (Musa AA, AAA, and AAAA Groups) and also hybrids (Musa x paradisiaca AB, AAB, ABB, ABBB, AAAB, AABB Groups). The part consumed/analysed is not specified. When relevant, information on the part consumed/analysed has to be reported with additional facet descriptors. In case of data collections related to legislations, the default part consumed/analysed is the one defined in the applicable legislation."}
          "A01LC Common banana"])))

(deftest parse-classifier
  (is (= (foodex2/parse-classifier "A01LH#F01.A064R$F02.A0ELC$F27.A01LH$F28.A07KQ")
         {:foodex2/term {:foodex2.term/code "A01LH"}
          :foodex2/aspects
          #{{:foodex2/facet {:foodex2.facet/id "F01"}
             :foodex2/term {:foodex2.term/code "A064R"}}
            {:foodex2/facet {:foodex2.facet/id "F02"}
             :foodex2/term {:foodex2.term/code "A0ELC"}}
            {:foodex2/facet {:foodex2.facet/id "F27"}
             :foodex2/term {:foodex2.term/code "A01LH"}}
            {:foodex2/facet {:foodex2.facet/id "F28"}
             :foodex2/term {:foodex2.term/code "A07KQ"}}}})))

(deftest make-classifier
  (is (= (foodex2/make-classifier {:foodex2/term {:foodex2.term/code "A01LH"}
                                       :foodex2/aspects
                                       #{{:foodex2/facet {:foodex2.facet/id "F01"}
                                          :foodex2/term {:foodex2.term/code "A064R"}}
                                         {:foodex2/facet {:foodex2.facet/id "F02"}
                                          :foodex2/term {:foodex2.term/code "A0ELC"}}
                                         {:foodex2/facet {:foodex2.facet/id "F27"}
                                          :foodex2/term {:foodex2.term/code "A01LH"}}
                                         {:foodex2/facet {:foodex2.facet/id "F28"}
                                          :foodex2/term {:foodex2.term/code "A07KQ"}}}})
         "A01LH#F01.A064R$F02.A0ELC$F27.A01LH$F28.A07KQ")))

(comment
  (require '[datomic-type-extensions.api :as d])
  (def foods-db matvaretabellen.dev/foods-db)
  (d/entity foods-db [:food/id "04.376"])

  )
