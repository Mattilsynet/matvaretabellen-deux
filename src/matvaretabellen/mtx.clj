(ns matvaretabellen.mtx
  "Rigg for å jobbe med filen MTX.ecf / MTX.xml fra EFSA."
  (:require [babashka.fs :as fs]
            [clojure.data.xml :as xml]
            [clojure.string :as str]))

(defn load-from-zip
  "From zip file <zip>, load contained xml file <entry>"
  [zip entry]
  (let [tempdir (fs/create-temp-dir)]
    (fs/delete-on-exit tempdir)
    (fs/unzip zip tempdir)
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

(defn parse-note
  "FoodEx2 notes may contain links, with funny formating. Example:

  Live animal of the taxonomic species Tylosurus choram, within the Family
  Belonidae. […] whole living organism.£http://www.fishbase.se/summary/Tylosurus-choram.html£http://www.marinespecies.org/aphia.php?p=taxlist&tName=Tylosurus choram

  We bestow an extra attribute upon such notes."
  [note-raw]
  (let [[text & hrefs] (str/split note-raw #"£")]
    (cond-> {:foodex2.term/note text}
      (seq hrefs) (assoc :foodex2.term/note-links (set hrefs)))))

(defn parse-term [term]
  (let [note (some-> (term-lookup term [:termDesc :termScopeNote]) parse-note)]
    (cond-> {:foodex2.term/code (term-lookup term [:termDesc :termCode])
             :foodex2.term/name (term-lookup term [:termDesc :termExtendedName])}
      note (merge note))))

(comment
  (set! *print-namespace-maps* false)

  (def mtx (load-from-zip "data/MTX.ecf" "MTX.xml"))
  (def terms (find-terms mtx))

  ;; View terms / parsed terms
  (count terms)
  (->> terms
       shuffle
       (take 10)
       (map parse-term))

  ;; Per 2025-11-26, 13 terms don't have a note.
  (->> terms
       (remove (comp :foodex2.term/note parse-term))
       first)

  )
