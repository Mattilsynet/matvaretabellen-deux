(ns matvaretabellen.dictionary-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [m1p.core :as m1p]
            [m1p.validation :as v]))

(defn load-dictionaries [locales]
  (->> (for [locale locales]
         [locale (-> (str "matvaretabellen/i18n/" (name locale) ".edn")
                     io/resource
                     slurp
                     read-string
                     m1p/prepare-dictionary)])
       (into {})))

(defn locale-lookup?
  "Interpolation discrepancies are not to worry about if the only discrepancy is
  that the dictionaries interpolate different locales - that is the exact reason
  why we have things like `:i18n/lookup`."
  [problem]
  (and (= :interpolation-discrepancy (:kind problem))
       (= #{:nb :en} (apply into (vals (:dictionaries problem))))))

(deftest dictionary-parity-test
  (testing "Ensures i18n dictionaries have similar enough content"
    (let [dicts (load-dictionaries [:nb :en])
          problems (->> (concat
                         (v/find-non-kw-keys dicts)
                         (v/find-unqualified-keys dicts)
                         (v/find-missing-keys dicts)
                         (v/find-misplaced-interpolations dicts)
                         (v/find-type-discrepancies dicts)
                         (v/find-interpolation-discrepancies dicts)
                         (v/find-fn-get-param-discrepancies dicts))
                        (remove locale-lookup?))]
      (is (empty? problems)
          (v/format-report dicts problems)))))
