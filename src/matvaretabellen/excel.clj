(ns matvaretabellen.excel
  (:import [org.apache.poi.xssf.usermodel XSSFWorkbook])
  (:require [broch.core :as b]
            [datomic-type-extensions.api :as d]))

(defn add-index [coll]
  (map-indexed (fn [i m] (assoc m :index i)) coll))

(defn create-bold-style [workbook]
  (let [font (.createFont workbook)
        style (.createCellStyle workbook)]
    (.setBold font true)
    (.setFont style font)
    style))

(defn create-excel-file [file-name sheets]
  (let [workbook (XSSFWorkbook.)
        bold-style (create-bold-style workbook)]
    (doseq [{:keys [title rows]} sheets]
      (let [sheet (.createSheet workbook title)]
        (doseq [{:keys [index cells bold?]} (add-index rows)]
          (let [row (.createRow sheet index)]
            (doseq [{:keys [index text]} (add-index cells)]
              (let [cell (.createCell row index)]
                (when bold? (.setCellStyle cell bold-style))
                (.setCellValue cell text)))))))

    (let [file-out (java.io.FileOutputStream. file-name)]
      (.write workbook file-out)
      (.close file-out))

    :done))

(defn get-food-fields [locale]
  [{:title "Matvare ID" :path [:food/id]}
   {:title "Matvare" :path [:food/name locale]}
   {:title "Spiselig del (%)" :path [:food/edible-part :measurement/percent]}
   {:title "Vann (g)" :constituent "Vann"}
   {:title "Kilojoule (kJ)" :path [:food/energy :measurement/quantity]}
   {:title "Kilokalorier (kcal)" :path [:food/calories :measurement/observation]}
   {:title "Fett (g)" :constituent "Fett"}
   {:title "Karbohydrater (g)" :constituent "Karbo"}
   {:title "Kostfiber (g)" :constituent "Fiber"}
   {:title "Protein (g)" :constituent "Protein"}
   {:title "Alkohol (g)" :constituent "Alko"}])

(defn get-constituent-value [food id]
  (some->> (:food/constituents food)
           (filter (comp #{id} :nutrient/id :constituent/nutrient))
           first
           :measurement/quantity
           b/num))

(defn get-scalar-at-path [food path]
  (let [v (get-in food path)]
    (cond-> v
      (instance? broch.impl.Quantity v)
      b/num)))

(defn prepare-food-cells [locale food]
  (for [{:keys [path constituent]} (get-food-fields locale)]
    {:text (str (cond
                  path (get-scalar-at-path food path)
                  constituent (get-constituent-value food constituent)))}))

(defn prepare-foods-header-row [locale]
  {:bold? true
   :cells (for [{:keys [title]} (get-food-fields locale)]
            {:text title})})

(defn prepare-foods-sheet [locale title foods]
  {:title title
   :rows (into [(prepare-foods-header-row locale)]
               (for [food (sort-by :food/id foods)]
                 {:cells (prepare-food-cells locale food)}))})

(comment

  (def db (d/db matvaretabellen.dev/conn))
  (def food (d/entity db [:food/id "06.531"]))

  (prepare-food-cells :nb food)

  (def foods [food])
  (def foods (for [eid (d/q '[:find [?e ...] :where [?e :food/id]] db)]
               (d/entity db eid)))

  (prepare-foods-sheet :nb "Matvarer" foods)

  (create-excel-file "test.xlsx" [(prepare-foods-sheet :nb "Matvarer" foods)])



  )
