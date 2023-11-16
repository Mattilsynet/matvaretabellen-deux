(ns matvaretabellen.excel
  (:require [broch.core :as b]
            [clojure.string :as str]
            [datomic-type-extensions.api :as d]
            [matvaretabellen.food :as food]
            [matvaretabellen.misc :as misc]
            [matvaretabellen.nutrient :as nutrient])
  (:import (java.io ByteArrayOutputStream FileOutputStream)
           (org.apache.poi.ss.util CellRangeAddress)
           (org.apache.poi.xssf.usermodel XSSFWorkbook)))

(defn add-index [coll]
  (map-indexed (fn [i m] (assoc m :index i)) coll))

(defn create-bold-style [workbook]
  (let [style (.createCellStyle workbook)
        font (.createFont workbook)]
    (.setBold font true)
    (.setFont style font)
    style))

(defn create-title-style [workbook]
  (let [style (.createCellStyle workbook)
        font (.createFont workbook)]
    (.setBold font true)
    (.setFontHeightInPoints font 16)
    (.setFont style font)
    style))

(defn count-columns [rows]
  (apply max 0 (map (comp count :cells) rows)))

(defn create-workbook [sheets]
  (let [workbook (XSSFWorkbook.)
        bold-style (create-bold-style workbook)
        title-style (create-title-style workbook)]
    (doseq [{:keys [title rows]} sheets]
      (let [sheet (.createSheet workbook title)
            column-count (count-columns rows)]
        (doseq [{:keys [index cells bold? title? merged?]} (add-index rows)]
          (let [row (.createRow sheet index)]
            (doseq [{:keys [index text]} (add-index cells)]
              (let [cell (.createCell row index)]
                (when bold? (.setCellStyle cell bold-style))
                (when title? (.setCellStyle cell title-style))
                (.setCellValue cell text)))
            (when merged?
              (.addMergedRegion sheet (CellRangeAddress. index index 0 (dec column-count))))))
        (doseq [i (range column-count)]
          (.autoSizeColumn sheet i))))
    workbook))

(defn create-excel-file [file-name workbook]
  (let [file-out (FileOutputStream. file-name)]
    (.write workbook file-out)
    (.close file-out))
  :done)

(defn create-excel-byte-array [workbook]
  (with-open [stream (ByteArrayOutputStream.)]
    (.write workbook stream)
    (.toByteArray stream)))

(defn get-basic-food-fields [db locale]
  [{:title "Matvare ID" :path [:food/id]}
   {:title "Matvare" :path [:food/name locale]}
   {:title "Spiselig del (%)" :measurement {:path [:food/edible-part]
                                            :field :measurement/percent}}
   (d/entity db [:nutrient/id "Vann"])
   {:title "Kilojoule (kJ)" :measurement {:path [:food/energy]
                                          :field :measurement/quantity}}
   {:title "Kilokalorier (kcal)" :measurement {:path [:food/calories]
                                               :field :measurement/observation}}
   (d/entity db [:nutrient/id "Fett"])
   (d/entity db [:nutrient/id "Karbo"])
   (d/entity db [:nutrient/id "Fiber"])
   (d/entity db [:nutrient/id "Protein"])
   (d/entity db [:nutrient/id "Alko"])])

(defn get-all-food-fields [db locale]
  (->> (d/q '[:find [?e ...] :where [?e :nutrient/id]]
            db)
       (map #(d/entity db %))
       (remove (comp empty? :nutrient/unit))
       (nutrient/sort-by-preference)
       (into [{:title "Matvare ID" :path [:food/id]}
              {:title "Matvare" :path [:food/name locale]}])))

(defn get-constituent [food nutrient-id]
  (some->> (:food/constituents food)
           (filter (comp #{nutrient-id} :nutrient/id :constituent/nutrient))
           first))

(defn get-scalar-at-path [food path]
  (let [v (get-in food path)]
    (cond-> v
      (instance? broch.impl.Quantity v)
      b/num)))

(defn prepare-food-cells [fields food]
  (for [{:keys [path measurement] :as f} fields]
    {:text (str (cond
                  path (get-scalar-at-path food path)
                  measurement (get-in food (conj (:path measurement) (:field measurement)))
                  (:nutrient/id f) (some-> (get-constituent food (:nutrient/id f))
                                           :measurement/quantity
                                           b/num)))}))

(defn prepare-reference-cells [fields food]
  (for [{:keys [path measurement] :as f} fields]
    (let [text (str (cond
                      path (get-scalar-at-path food path)
                      measurement (get-in food (into (:path measurement) [:measurement/origin :origin/id]))
                      (:nutrient/id f) (some-> (get-constituent food (:nutrient/id f))
                                               :measurement/origin
                                               :origin/id)))]
      (cond-> {:text text}
        (not path) (assoc :origin/id text)))))

(defn prepare-foods-header-row [fields locale]
  {:bold? true
   :cells (for [field fields]
            {:text (or (:title field)
                       (str (get-in field [:nutrient/name locale])
                            " (" (:nutrient/unit field) ")"))})})

(defn get-top-group [food-group]
  (if-let [parent (:food-group/parent food-group)]
    (get-top-group parent)
    food-group))

(defn prepare-food-rows [locale fields foods prep]
  (->> (group-by (comp get-top-group :food/food-group) foods)
       (sort-by (comp :food-group/id first))
       (mapcat
        (fn [[group foods]]
          (into [{:merged? true :title? true
                  :cells [{:text (get-in group [:food-group/name locale])}]}]
                (for [food (sort-by :food/id foods)]
                  {:cells (prep fields food)}))))))

(defn prepare-foods-sheet [locale title fields foods]
  {:title title
   :rows (into [(prepare-foods-header-row fields locale)]
               (prepare-food-rows locale fields foods prepare-food-cells))})

(defn prepare-reference-sheet [locale title fields foods]
  {:title title
   :rows (into [(prepare-foods-header-row fields locale)]
               (prepare-food-rows locale fields foods prepare-reference-cells))})

(defn prepare-reference-lookup-sheet [db locale title reference-sheet]
  {:title title
   :rows (for [origin-id (->> (:rows reference-sheet)
                              (mapcat :cells)
                              (keep :origin/id)
                              set
                              (sort-by misc/natural-order-comparator-ish))]
           {:cells [{:text origin-id}
                    {:text (get-in (d/entity db [:origin/id origin-id])
                                   [:origin/description locale])}]})})

(def cover-sheet-text
  {:nb [{:header? true :cells [{:text "Matvaretabellen {:year}"}]}
        ""
        :preamble
        ""
        "Informasjonen er fordelt på fem forskjellige ark:"
        ""
        "- I arket Matvarer finner du de viktigste næringsstoffverdiene for hver matvare."
        "- I arket Matvarer (alle næringsstoffer) finner du en oversikt med alle næringsstoffene for hver matvare."
        "- Til hvert av disse arkene finnes et vedleggsark med kilder. Disse forteller hvordan næringsstoffverdiene er funnet."
        "- Til slutt finner du arket Kildeoppslag med en beskrivelse av hver kilde."
        ""
        "Matvaretabellen er en tjeneste fra Mattilsynet."
        "Husk å oppgi kilde når du bruker tabellverdiene: Matvaretabellen {:year} Mattilsynet, www.matvaretabellen.no"
        "Du kan alltid finne nyeste versjon på matvaretabellen.no"
        #_""
        #_"PS! Har du behov for å behandle disse dataene programatisk er de samme verdiene tilgjengelig som JSON og EDN på matvaretabellen.no"]
   :en [{:header? true :cells [{:text "The Norwegian Food Composition Table {:year}"}]}
        ""
        :preamble
        ""
        "The information is spread across five sheets:"
        ""
        "- The Foods sheet details key nutrient values for each food item."
        "- The Foods (all nutrients) sheet offers a comprehensive list of all nutrients for each food item."
        "- Each of these sheets is accompanied by a supplementary sheet listing sources, explaining how the nutrient values were determined."
        "- Additionally, the Source Lookup sheet describes each source in detail."
        ""
        "The Food Composition Table is a service provided by the Norwegian Food Safety Authority."
        "Remember to cite the source when using the table values: The Norwegian Food Composition Table {:year} Norwegian Food Safety Authority, www.matvaretabellen.no"
        "The latest version is always available at matvaretabellen.no/en/"
        #_""
        #_"PS! For programmatic processing of this data, the same values are accessible in JSON and EDN formats at matvaretabellen.no/en/"]})

(def i18n
  {:information {:nb "Informasjon"
                 :en "Information"}
   :foods {:nb "Matvarer"
           :en "Foods"}
   :sources {:nb "Kilder"
             :en "Sources"}
   :foods-all-nutrients {:nb "Matvarer (alle næringsstoffer)"
                         :en "Foods (all nutrients)"}
   :sources-all-nutrientes {:nb "Kilder (alle næringsstoffer)"
                            :en "Sources (all nutrients)"}
   :source-lookup {:nb "Kildeoppslag"
                   :en "Source Lookup"}})

(defn prepare-foods-cover-sheet [locale year preamble]
  {:title (-> i18n :information locale)
   :rows (for [line (cover-sheet-text locale)]
           (-> (cond
                 (string? line) {:cells [{:text line}]}
                 (= :preamble line) {:cells [{:text preamble}]}
                 (map? line) line)
               (update-in [:cells 0 :text] str/replace "{:year}" (str year))))})

(defn prepare-food-sheets [db locale year preamble foods]
  (let [basic-fields (get-basic-food-fields db locale)
        all-fields (get-all-food-fields db locale)
        reference-sheet (prepare-reference-sheet locale (-> i18n :sources-all-nutrientes locale) all-fields foods)]
    [(prepare-foods-cover-sheet locale year preamble)
     (prepare-foods-sheet locale (-> i18n :foods locale) basic-fields foods)
     (prepare-reference-sheet locale (-> i18n :sources locale) basic-fields foods)
     (prepare-foods-sheet locale (-> i18n :foods-all-nutrients locale) all-fields foods)
     reference-sheet
     (prepare-reference-lookup-sheet db locale (-> i18n :source-lookup locale) reference-sheet)]))

(defn render-some-foods [db year page preamble foods]
  (let [locale (:page/locale page)]
    {:status 200
     :headers {"Content-Type" "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}
     :body (create-excel-byte-array
            (create-workbook
             (prepare-food-sheets db locale year (get preamble locale) foods)))}))

(defn render-all-foods [db year page]
  (let [foods (for [eid (d/q '[:find [?e ...] :where [?e :food/id]] db)]
                (d/entity db eid))]
    (render-some-foods db year page
                       {:nb (str "Her finner du informasjon om alle " (count foods) " matvarene i Matvaretabellen.")
                        :en (str "This document provides information about all " (count foods) " foods listed in the Norwegian Food Composition Table.")}
                       foods)))

(defn render-food-group-foods [db year page]
  (let [food-group (d/entity db [:food-group/id (:page/food-group-id page)])
        food-group-name (get-in food-group [:food-group/name (:page/locale page)])
        foods (food/get-all-food-group-foods food-group)]
    (render-some-foods db year page
                       {:nb (str "Her finner du informasjon om de " (count foods) " matvarene under " food-group-name " i Matvaretabellen.")
                        :en (str "This document provides information about the " (count foods) " foods under " food-group-name " listed in the Norwegian Food Composition Table.")}
                       foods)))

(defn render-nutrient-foods [db year page]
  (let [nutrient (d/entity db [:nutrient/id (:page/nutrient-id page)])
        nutrient-name (str/lower-case (get (:nutrient/name nutrient) (:page/locale page)))
        foods (nutrient/get-foods-by-nutrient-density nutrient)]
    (render-some-foods db year page
                       {:nb (str "Her finner du informasjon om de " (count foods) " matvarene med " nutrient-name " i Matvaretabellen.")
                        :en (str "This document provides information about the " (count foods) " foods with " nutrient-name " listed in the Norwegian Food Composition Table.")}
                       foods)))

(comment

  (def db (d/db matvaretabellen.dev/conn))
  (def food (d/entity db [:food/id "06.531"]))
  (def locale :nb)
  (def fields (get-basic-food-fields db locale))

  (prepare-foods-cover-sheet :nb 2023 "Her finner du informasjon om næringsstoffer i matvarene i Matvaretabellen.")

  (prepare-food-cells fields food)

  (def foods [food])
  (def foods (for [eid (d/q '[:find [?e ...] :where [?e :food/id]] db)]
               (d/entity db eid)))

  (set (keep :origin/id (mapcat :cells (:rows (prepare-reference-sheet locale "Referanser" fields foods)))))

  (prepare-foods-sheet locale "Matvarer" (get-basic-food-fields db locale) foods)
  (prepare-foods-sheet locale "Matvarer (alle næringsstoffer)" (get-all-food-fields db locale) foods)

  (create-excel-file "test.xlsx" (create-workbook (prepare-food-sheets db locale 2023 "" foods)))

  )
