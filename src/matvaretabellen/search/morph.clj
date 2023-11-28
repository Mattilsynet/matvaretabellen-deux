(ns matvaretabellen.search.morph
  "Morphological analysis based on data from
  https://www.nb.no/sprakbanken/ressurskatalog/oai-nb-no-sbr-5/"
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def header-ks
  {"LOEPENR" :løpenr
   "LEDDANALYSE_ID" :leddanalyse-id
   "OPPSLAG" :oppslag
   "LEDDANALYSE" :leddanalyse
   "FORLEDD" :forledd
   "FORLEDD_GRAM" :forledd-gram
   "FUGE" :fuge
   "ETTERLEDD" :etterledd
   "ETTERLEDD_GRAM" :etterledd-gram
   "LEDDMARKERT_BOB" :leddmarkert-bob
   "NEG_FUGE" :neg-fuge
   "OPPSLAG_LEDD_MARKERT" :oppslag-ledd-markert
   "BINDESTREKSFORM_FOR_LEMMA" :bindestreksform-for-lemma
   "LEMMA_ID" :lemma-id})

(defonce analysis-data
  (let [[header-text & lines] (str/split-lines (slurp (io/resource "leddanalyse.txt")))
        headers (map header-ks (str/split header-text #"\t"))]
    (->> lines
         (map #(->> (str/split % #"\t")
                    (map str/lower-case)
                    (map vector headers)
                    (into {})))
         (map (juxt :oppslag identity))
         (into {}))))

(defn split-compound-word [s]
  (let [{:keys [forledd etterledd]} (get analysis-data (str/lower-case s))]
    (remove empty? [forledd etterledd])))

(defn get-indexable-words [s]
  (set (conj (split-compound-word s) (str/lower-case s))))
