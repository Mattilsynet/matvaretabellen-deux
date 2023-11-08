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

(defn get-nutrient-url [locale nutrient-or-name]
  (->> (if (:nutrient/name nutrient-or-name)
         (get-in nutrient-or-name [:nutrient/name locale])
         nutrient-or-name)
       (get-url locale "")))

(defn get-food-group-url [locale the-name]
  (get-url locale (case locale
                    :nb "gruppe/"
                    :en "group/") the-name))

(comment

  (slugify "Müsli og grøt fra ørten 5gr D12 vitamin kål")

  )
