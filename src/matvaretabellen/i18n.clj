(ns matvaretabellen.i18n
  (:require [clojure.string :as str])
  (:import (java.text NumberFormat)
           (java.util Locale)))

(def locales
  {:nb (Locale/forLanguageTag "nb-NO")
   :en (Locale/forLanguageTag "en-GB")})

(defn format-number [locale n & [{:keys [decimals]}]]
  (let [formatter (NumberFormat/getNumberInstance (locales locale))]
    (when decimals
      (.setMaximumFractionDigits formatter decimals))
    (.format formatter n)))

(defn m1p-fn-num [{:keys [locale]} _params n & [opt]]
  (format-number locale n opt))

(def and-word
  {:nb "og"
   :en "and"})

(defn enumerate [locale xs]
  (if (< 1 (count xs))
    (str (str/join ", " (butlast xs)) " " (and-word locale) " " (last xs))
    (str/join xs)))

(defn m1p-fn-enumerate [{:keys [locale]} _ xs & _args]
  (enumerate locale xs))
