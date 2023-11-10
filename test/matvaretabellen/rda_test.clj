(ns matvaretabellen.rda-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.foodcase-import :as foodcase-import]
            [matvaretabellen.rda :as sut]))

(def csv
  (->> [";Database ID;Rekkefølge;V1: Hovedtype bruker;V2: Alder osv;REE;Ekstra energiforbruk;V3a/b: Arbeid/hovedaktivitet;V4: Fritid ;PAL arbeid;PAL fra fritid;PAL (sum);MJ/dag;kJ;kcal;KH (E %) (min);KH (E %) (max);Tilsatt sukker (E %) (max);Kostfiber (g) ;Kostfiber (g) ;Fett ( E %) ;Fett (E %);Mettet fett (E %) ; Transfett (E %);Enumettet fett ( E %) (min);Enumettet fett (E %)(max);Flerumettet fett (E %) (min);Flerumettet fett (E %)(max);Omega-3 (E %);Omega-6 (E %);Protein (E %) (min);Protein (E %) (max);Alkhol (g);Salt (g);Vitamin A;Vitamin D;Vitamin E;Tiamin;Riboflavin;Niacin;Niacinekvivalenter;Vitamin B6;Folat;Vitamin B12;Vitamin C;Kalsium;Jern;Kalium;Fosfor;Natrium (max);Magnesium;Sink;Kobber;Selen;Jod\r"
        ";;;nutrient ID (matvaretabellen);;;;;;;;;;Energi1;Energi2;Karbo;Karbo;Sukker;Fiber;Fiber;Fett;Fett;Mettet;Trans;Enumet;Enumet;Flerum;Flerum;Omega-3;Omega-6;Protein;Protein;Alko;NaCl;Vit A;Vit D;Vit E;Vit B1;Vit B2;Niacin;;Vit B6;Folat;Vit B12;Vit C;Ca;Fe;K;P;Na;Mg;Zn;Cu;Se;I\r"
        ";;;Grenser;;;;;;;;;;gjsn;gjsn;min;max;gjsn;min;max;min;max;gjsn;gjsn;min;max;min;max;gjsn;gjsn;min;max;gjsn;gjsn;gjsn;gjsn;gjsn;gjsn;gjsn;gjsn;;gjsn;gjsn;gjsn;gjsn;gjsn;gjsn;gjsn;gjsn;gjsn;gjsn;gjsn;gjsn;gjsn;gjsn\r"
        ";129;18;Gutt;6-9 år;4,535;;Lavt aktivitetsnivå;;;;1,42;6,44;6439,7;1539,0883;45;60;<10;13;19;25;40;<10;<1;10;20;5;10;1;5;10;20;;<3,5;400;10;6;0,9;1,1;;;1;130;1,3;40;700;9;2000;540;<1400;200;7;0,5;30;120\r"
        "Eksempelmeny;130;19;Gutt;6-9 år;4,535;;Gjennomsnittlig aktivitetsnivå;;;;1,57;7,12;7119,95;1701,66805;45;60;<10;14;21;25;40;<10;<1;10;20;5;10;1;5;10;20;;<3,5;400;10;6;0,9;1,1;;;1;130;1,3;40;700;9;2000;540;<1400;200;7;0,5;30;120\r"
        ";131;20;Gutt;6-9 år;4,535;;Høyt aktivitetsnivå;;;;1,69;7,66;7664,15;1831,73185;45;60;<10;15;23;25;40;<10;<1;10;20;5;10;1;5;10;20;;<3,5;400;10;6;0,9;1,1;;;1;130;1,3;40;700;9;2000;540;<1400;200;7;0,5;30;120\r"
        "Ingen eksempelmeny;132;6;Jente;6-9 år;4,22;;Lavt aktivitetsnivå;;;;1,42;5,99;5992,4;1432,1836;45;60;<10;12;18;25;40;<10;<1;10;20;5;10;1;5;10;20;;<3,5;400;10;6;0,9;1,1;;;1;130;1,3;40;700;9;2000;540;<1400;200;7;0,5;30;120\r"
        ";133;7;Jente;6-9 år;4,22;;Gjennomsnittlig aktivitetsnivå;;;;1,57;6,63;6625,4;1583,4706;45;60;<10;13;20;25;40;<10;<1;10;20;5;10;1;5;10;20;;<3,5;400;10;6;0,9;1,1;;;1;130;1,3;40;700;9;2000;540;<1400;200;7;0,5;30;120\r"
        ";134;8;Jente;6-9 år;4,22;;Høyt aktivitetsnivå;;;;1,69;7,13;7131,8;1704,5002;45;60;<10;14;21;25;40;<10;<1;10;20;5;10;1;5;10;20;;<3,5;400;10;6;0,9;1,1;;;1;130;1,3;40;700;9;2000;540;<1400;200;7;0,5;30;120\r"
        ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;\r"
        ";135;21;Gutt;10-13 år;5,3825;;Lavt aktivitetsnivå;;;;1,66;8,93;8934,95;2135,45305;45;60;<10;18;27;25;40;<10;<1;10;20;5;10;1;5;10;20;;<6;600;10;8;1,1;1,3;;;1,3;200;2;50;900;11;3300;700;<2300;280;11;0,7;40;150\r"
        ";136;22;Gutt;10-13 år;5,3825;;Gjennomsnittlig aktivitetsnivå;;;;1,73;9,31;9311,725;2225,502275;45;60;<10;19;28;25;40;<10;<1;10;20;5;10;1;5;10;20;;<6;600;10;8;1,1;1,3;;;1,3;200;2;50;900;11;3300;700;<2300;280;11;0,7;40;150\r"
        ";137;23;Gutt;10-13 år;5,3825;;Høyt aktivitetsnivå;;;;1,85;9,96;9957,625;2379,872375;45;60;<10;20;30;25;40;<10;<1;10;20;5;10;1;5;10;20;;<6;600;10;8;1,1;1,3;;;1,3;200;2;50;900;11;3300;700;<2300;280;11;0,7;40;150\r"
        ";138;9;Jente;10-13 år;4,9975;;Lavt aktivitetsnivå;;;;1,66;8,30;8295,85;1982,70815;45;60;<10;17;25;25;40;<10;<1;10;20;5;10;1;5;10;20;;<6;600;10;7;1;1,2;;;1,1;200;2;50;900;11;3100;700;<2300;280;8;0,7;40;150\r"
        ";139;10;Jente;10-13 år;4,9975;;Gjennomsnittlig aktivitetsnivå;;;;1,73;8,65;8645,675;2066,316325;45;60;<10;17;26;25;40;<10;<1;10;20;5;10;1;5;10;20;;<6;600;10;7;1;1,2;;;1,1;200;2;50;900;11;3100;700;<2300;280;8;0,7;40;150\r"
        ";140;11;Jente;10-13 år;4,9975;;Høyt aktivitetsnivå;;;;1,85;9,25;9245,375;2209,644625;45;60;<10;18;28;25;40;<10;<1;10;20;5;10;1;5;10;20;;<6;600;10;7;1;1,2;;;1,1;200;2;50;900;11;3100;700;<2300;280;8;0,7;40;150\r"]
       (str/join "\n")))

(def test-conn (atom nil))

(defn get-foods-db []
  (when-not @test-conn
    (reset! test-conn (foodcase-import/create-data-database "datomic:mem://rda-test")))
  (d/db @test-conn))

(deftest parse-csv-test
  (testing "Builds rda datastructure based on loose information in headers"
    (is (= (->> (sut/read-csv (get-foods-db) csv)
                (drop 5)
                first)
           {:rda/id "rda1743657494"
            :rda/order 8
            :rda/demographic {:nb "Jente, 6-9 år"
                              :en "Girl, 6-9 years"}
            :rda/energy-recommendation #broch/quantity[7131.8 "kJ"]
            :rda/kcal-recommendation 1704.5002
            :rda/work-activity-level {:nb "Høyt aktivitetsnivå"
                                      :en "High activity level"}
            :rda/recommendations
            #{{:rda.recommendation/nutrient-id "Karbo"
               :rda.recommendation/min-energy-pct 45
               :rda.recommendation/max-energy-pct 60}
              {:rda.recommendation/nutrient-id "Sukker"
               :rda.recommendation/max-energy-pct 10}
              {:rda.recommendation/nutrient-id "Fiber"
               :rda.recommendation/min-amount #broch/quantity[14.0 "g"]
               :rda.recommendation/max-amount #broch/quantity[21.0 "g"]}
              {:rda.recommendation/nutrient-id "Fett"
               :rda.recommendation/min-energy-pct 25
               :rda.recommendation/max-energy-pct 40}
              {:rda.recommendation/nutrient-id "Mettet"
               :rda.recommendation/max-energy-pct 10}
              {:rda.recommendation/nutrient-id "Trans"
               :rda.recommendation/max-energy-pct 1}
              {:rda.recommendation/nutrient-id "Enumet"
               :rda.recommendation/min-energy-pct 10
               :rda.recommendation/max-energy-pct 20}
              {:rda.recommendation/nutrient-id "Flerum"
               :rda.recommendation/min-energy-pct 5
               :rda.recommendation/max-energy-pct 10}
              {:rda.recommendation/nutrient-id "Omega-3"
               :rda.recommendation/average-energy-pct 1}
              {:rda.recommendation/nutrient-id "Omega-6"
               :rda.recommendation/average-energy-pct 5}
              {:rda.recommendation/nutrient-id "Protein"
               :rda.recommendation/min-energy-pct 10
               :rda.recommendation/max-energy-pct 20}
              {:rda.recommendation/nutrient-id "Alko"}
              {:rda.recommendation/nutrient-id "NaCl"
               :rda.recommendation/max-amount #broch/quantity[3.5 "g"]}
              {:rda.recommendation/nutrient-id "Vit A"
               :rda.recommendation/average-amount #broch/quantity[400.0 "µg-RE"]}
              {:rda.recommendation/nutrient-id "Vit D"
               :rda.recommendation/average-amount #broch/quantity[10.0 "µg"]}
              {:rda.recommendation/nutrient-id "Vit E"
               :rda.recommendation/average-amount #broch/quantity[6.0 "mg-ATE"]}
              {:rda.recommendation/nutrient-id "Vit B1"
               :rda.recommendation/average-amount #broch/quantity[0.9 "mg"]}
              {:rda.recommendation/nutrient-id "Vit B2"
               :rda.recommendation/average-amount #broch/quantity[1.1 "mg"]}
              {:rda.recommendation/nutrient-id "Niacin"}
              {:rda.recommendation/nutrient-id "Vit B6"
               :rda.recommendation/average-amount #broch/quantity[1.0 "mg"]}
              {:rda.recommendation/nutrient-id "Folat"
               :rda.recommendation/average-amount #broch/quantity[130.0 "µg"]}
              {:rda.recommendation/nutrient-id "Vit B12"
               :rda.recommendation/average-amount #broch/quantity[1.3 "µg"]}
              {:rda.recommendation/nutrient-id "Vit C"
               :rda.recommendation/average-amount #broch/quantity[40.0 "mg"]}
              {:rda.recommendation/nutrient-id "Ca"
               :rda.recommendation/average-amount #broch/quantity[700.0 "mg"]}
              {:rda.recommendation/nutrient-id "Fe"
               :rda.recommendation/average-amount #broch/quantity[9.0 "mg"]}
              {:rda.recommendation/nutrient-id "K"
               :rda.recommendation/average-amount #broch/quantity[2000.0 "mg"]}
              {:rda.recommendation/nutrient-id "P"
               :rda.recommendation/average-amount #broch/quantity[540.0 "mg"]}
              {:rda.recommendation/nutrient-id "Na"
               :rda.recommendation/max-amount #broch/quantity[1400.0 "mg"]}
              {:rda.recommendation/nutrient-id "Mg"
               :rda.recommendation/average-amount #broch/quantity[200.0 "g"]}
              {:rda.recommendation/nutrient-id "Zn"
               :rda.recommendation/average-amount #broch/quantity[7.0 "mg"]}
              {:rda.recommendation/nutrient-id "Cu"
               :rda.recommendation/average-amount #broch/quantity[0.5 "mg"]}
              {:rda.recommendation/nutrient-id "Se"
               :rda.recommendation/average-amount #broch/quantity[30.0 "µg"]}
              {:rda.recommendation/nutrient-id "I"
               :rda.recommendation/average-amount #broch/quantity[120.0 "µg"]}}})))

  (testing "Creates one page per demographic"
    (is (= (->> (sut/read-csv (get-foods-db) csv)
                (take 3)
                (sut/get-rda-pages [:nb :en]))
           [{:page/uri "/rda/nb/rda1521056637.json"
             :page/kind :page.kind/rda-profile
             :page/locale :nb
             :page/rda-id "rda1521056637"}
            {:page/uri "/rda/en/rda1521056637.json"
             :page/kind :page.kind/rda-profile
             :page/locale :en
             :page/rda-id "rda1521056637"}
            {:page/uri "/rda/nb/rda-1532942700.json"
             :page/kind :page.kind/rda-profile
             :page/locale :nb
             :page/rda-id "rda-1532942700"}
            {:page/uri "/rda/en/rda-1532942700.json"
             :page/kind :page.kind/rda-profile
             :page/locale :en
             :page/rda-id "rda-1532942700"}
            {:page/uri "/rda/nb/rda1696251676.json"
             :page/kind :page.kind/rda-profile
             :page/locale :nb
             :page/rda-id "rda1696251676"}
            {:page/uri "/rda/en/rda1696251676.json"
             :page/kind :page.kind/rda-profile
             :page/locale :en
             :page/rda-id "rda1696251676"}]))))

(deftest json-conversion-test
  (testing "Simplifies data for JSON export"
    (is (= (->> (sut/read-csv (get-foods-db) csv)
                first
                (sut/->json :nb))
           {:id "rda1521056637"
            :demographic "Gutt, 6-9 år"
            :energy-recommendation {:n 6439.7 :symbol "kJ"}
            :kcal-recommendation 1539.0883
            :work-activity-level "Lavt aktivitetsnivå"
            :recommendations
            #{{:nutrient-id "Alko"}
              {:nutrient-id "Ca"
               :average-amount {:n 700.0 :symbol "mg"}}
              {:nutrient-id "Cu"
               :average-amount {:n 0.5 :symbol "mg"}}
              {:nutrient-id "Enumet"
               :min-energy-pct 10 :max-energy-pct 20}
              {:nutrient-id "Fe"
               :average-amount {:n 9.0 :symbol "mg"}}
              {:nutrient-id "Fett"
               :min-energy-pct 25
               :max-energy-pct 40}
              {:nutrient-id "Fiber"
               :min-amount {:n 13.0 :symbol "g"}
               :max-amount {:n 19.0 :symbol "g"}}
              {:nutrient-id "Flerum"
               :min-energy-pct 5
               :max-energy-pct 10}
              {:nutrient-id "Folat"
               :average-amount {:n 130.0 :symbol "µg"}}
              {:nutrient-id "I"
               :average-amount {:n 120.0 :symbol "µg"}}
              {:nutrient-id "K"
               :average-amount {:n 2000.0 :symbol "mg"}}
              {:nutrient-id "Karbo"
               :min-energy-pct 45 :max-energy-pct 60}
              {:nutrient-id "Mettet"
               :max-energy-pct 10}
              {:nutrient-id "Mg"
               :average-amount {:n 200.0 :symbol "g"}}
              {:nutrient-id "Na"
               :max-amount {:n 1400.0 :symbol "mg"}}
              {:nutrient-id "NaCl"
               :max-amount {:n 3.5 :symbol "g"}}
              {:nutrient-id "Niacin"}
              {:nutrient-id "Omega-3"
               :average-energy-pct 1}
              {:nutrient-id "Omega-6"
               :average-energy-pct 5}
              {:nutrient-id "P"
               :average-amount {:n 540.0 :symbol "mg"}}
              {:nutrient-id "Protein"
               :min-energy-pct 10 :max-energy-pct 20}
              {:nutrient-id "Se"
               :average-amount {:n 30.0 :symbol "µg"}}
              {:nutrient-id "Sukker"
               :max-energy-pct 10}
              {:nutrient-id "Trans"
               :max-energy-pct 1}
              {:nutrient-id "Vit A"
               :average-amount {:n 400.0 :symbol "µg-RE"}}
              {:nutrient-id "Vit B1"
               :average-amount {:n 0.9 :symbol "mg"}}
              {:nutrient-id "Vit B12"
               :average-amount {:n 1.3 :symbol "µg"}}
              {:nutrient-id "Vit B2"
               :average-amount {:n 1.1 :symbol "mg"}}
              {:nutrient-id "Vit B6"
               :average-amount {:n 1.0 :symbol "mg"}}
              {:nutrient-id "Vit C"
               :average-amount {:n 40.0 :symbol "mg"}}
              {:nutrient-id "Vit D"
               :average-amount {:n 10.0 :symbol "µg"}}
              {:nutrient-id "Vit E"
               :average-amount {:n 6.0 :symbol "mg-ATE"}}
              {:nutrient-id "Zn"
               :average-amount {:n 7.0 :symbol "mg"}}}}))))
