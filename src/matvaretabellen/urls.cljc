(ns matvaretabellen.urls
  (:require [clojure.string :as str])
  #?(:clj (:import (java.text Normalizer))))

(defn remove-diacritics [s]
  (-> #?(:clj (Normalizer/normalize s java.text.Normalizer$Form/NFD)
         :cljs (.normalize s "NFD"))
      (str/replace #"[\u0300-\u036F]" "")
      (str/replace #"æ" "ae")
      (str/replace #"ø" "o")))

(defn slugify [s]
  (-> (str/lower-case s)
      remove-diacritics
      (str/replace #"\W+" "-")))

(defn get-url [locale food-name]
  (str (when-not (= :nb locale)
         (str "/" (name locale)))
       "/" (slugify food-name) "/"))

(comment

  (slugify "Müsli og grøt fra ørten 5gr D12 vitamin kål")

  )
