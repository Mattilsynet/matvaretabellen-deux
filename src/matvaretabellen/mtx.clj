(ns matvaretabellen.mtx
  "Rigg for å jobbe med filen MTX.ecf / MTX.xml fra EFSA."
  (:require [babashka.fs :as fs]
            [clojure.data.xml :as xml]))

(defn load-from-zip
  "From zip file <zip>, load contained xml file <entry>"
  [zip entry]
  (let [tempdir (fs/create-temp-dir)]
    (fs/delete-on-exit tempdir)
    (fs/unzip zip tempdir)
    (fs/list-dir tempdir)
    (xml/parse-str (slurp (fs/file tempdir entry)))))

(defn select [tag xml]
  (filter (comp #{tag} :tag)
          (:content xml)))

(defn query [path xml]
  (reduce (fn [xmls tag]
            (mapcat (fn [xml]
                      (select tag xml)) xmls))
          [xml]
          path))

(defn find-terms [mtx]
  (query [:catalogue :catalogueTerms :term] mtx))

(defn term-lookup [term path]
  (->> (query path term)
       first :content first))

(defn parse-term [term]
  {:foodex2.term/code (term-lookup term [:termDesc :termCode])
   :foodex2.term/name (term-lookup term [:termDesc :termExtendedName])
   :foodex2.term/note (term-lookup term [:termDesc :termScopeNote])})

(comment
  (def mtx (load-from-zip "data/MTX.ecf" "MTX.xml"))
  (def terms (find-terms mtx))

  (count terms)

  (->> terms
       shuffle
       (take 10)
       (map parse-term))

  )
