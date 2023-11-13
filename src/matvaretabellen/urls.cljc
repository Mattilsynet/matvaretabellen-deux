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
      (str/replace #"\W+" "-")
      (str/replace #"\W+$" "")))

(defn get-base-url [locale]
  (str "/" (when-not (= :nb locale)
             (str (name locale) "/"))))

(defn get-url [locale prefix the-name]
  (str (get-base-url locale) prefix (slugify the-name) "/"))

(defn get-food-url [locale food-or-name]
  (->> (if-let [food-name (:food/name food-or-name)]
         (get food-name locale)
         food-or-name)
       (get-url locale "")))

(defn get-nutrients-url [locale]
  (str (get-base-url locale)
       (case locale
         :nb "naeringsstoffer/"
         :en "nutrients/")))

(defn get-nutrient-url [locale nutrient-or-name]
  (->> (if (:nutrient/name nutrient-or-name)
         (get-in nutrient-or-name [:nutrient/name locale])
         nutrient-or-name)
       (get-url locale "")))

(defn get-food-groups-url [locale]
  (str (get-base-url locale)
       (case locale
         :nb "matvaregrupper/"
         :en "food-groups/")))

(defn get-food-group-url [locale group-or-name]
  (let [the-name (if (:food-group/name group-or-name)
                   (get-in group-or-name [:food-group/name locale])
                   group-or-name)]
    (get-url locale (case locale
                      :nb "gruppe/"
                      :en "group/") the-name)))

(defn get-food-group-excel-url [locale group-or-name]
  (str/replace (get-food-group-url locale group-or-name)
               #"/$" ".xlsx"))

(comment

  (slugify "Müsli og grøt fra ørten 5gr D12 vitamin kål")

  )
