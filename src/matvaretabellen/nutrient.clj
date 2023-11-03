(ns matvaretabellen.nutrient)

(def nutrient-names
  {"C12:0Laurinsyre" {:nb "Laurinsyre (C12:0)" :en "Lauric Acid"}
   "C14:0Myristinsyre" {:nb "Myristinsyre (C14:0)" :en "Myristic Acid"}
   "C16:0Palmitinsyre" {:nb "Palmitinsyre (C16:0)" :en "Palmitic Acid"}
   "C16:1" {:nb "Palmitoleinsyre (C16:1)" :en "Palmitoleic Acid"}
   "C18:0Stearinsyre" {:nb "Stearinsyre (C18:0)" :en "Stearic Acid"}
   "C18:1" {:nb "C18:1" :en "Oleic Acid"}
   "C18:2n-6Linolsyre" {:nb "Linolsyre (C18:2n-6)" :en "Linoleic Acid"}
   "C18:3n-3AlfaLinolensyre" {:nb "Alfalinolensyre (ALA, C18:3n-3)" :en "Alpha-Linolenic Acid (ALA)"}
   "C20:3n-3Eikosatriensyre" {:nb "Eikosatriensyre (C20:3n-3)" :en "Eicosatrienoic Acid"}
   "C20:3n-6DihomoGammaLinolensyre" {:nb "C20:3n-6" :en "Dihomo-Gamma-Linolenic Acid (DGLA)"}
   "C20:4n-3Eikosatetraensyre" {:nb "Eikosatetraensyre (C20:4n-3)" :en "Eicosatetraenoic Acid"}
   "C20:4n-6Arakidonsyre" {:nb "Arakidonsyre (C20:4n-6)" :en "Arachidonic Acid"}
   "C20:5n-3Eikosapentaensyre" {:nb "Eikosapentaensyre (EPA, C20:5n-3)" :en "Eicosapentaenoic Acid (EPA)"}
   "C22:5n-3Dokosapentaensyre" {:nb "Dokosapentaensyre (DPA, C22:5n-3)" :en "Docosapentaenoic Acid (DPA)"}
   "C22:6n-3Dokosaheksaensyre" {:nb "Dokosaheksaensyre (DHA, C22:6n-3)" :en "Docosahexaenoic Acid (DHA)"}
   "Niacin" {:nb "Niacin (B3)" :en "Niacin (B3)"}
   "Folat" {:nb "Folat (B9)" :en "Folat (B9)"}
   "Retinol" {:nb "Vitamin A (Retinol)" :en "Vitamin A (Retinol)"}
   "B-karo" {:nb "Vitamin A (Betakaroten)" :en "Vitamin A (Beta-carotene)"}})

(defn get-name [nutrient]
  (or (get nutrient-names (:nutrient/id nutrient))
      (:nutrient/name nutrient)))

(def sort-names
  (->> ["Fett"
        "Karbo"
        "Protein"
        "Mettet"
        "Enumet"
        "Flerum"
        "Trans"
        "Kolest"]
       (map-indexed #(vector %2 (format " %02d" %1)))
       (into
        {"Niacin" "Vit B03"
         "Folat" "Vit B09"
         "Vit B1" "Vit B01"
         "Vit B2" "Vit B02"
         "Vit B6" "Vit B06"
         "Retinol" "A Retinol"
         "B-karo" "A Betakaroten"})))

(defn sort-by-preference [nutrients]
  (->> nutrients
       (sort-by (comp #(sort-names % %) :nutrient/id))))

(def apriori-groups
  (->> [{:nutrient/id "WaterSolubleVitamins"
         :nutrient/name {:nb "Vannløselige Vitaminer"
                         :en "Water-soluble Vitamins"}
         ::nutrient-ids ["Vit B1"
                         "Vit B12"
                         "Vit B2"
                         "Folat"
                         "Niacin"
                         "Vit B6"
                         "Vit C"]}
        {:nutrient/id "FatSolubleVitamins"
         :nutrient/name {:nb "Fett-løselige Vitaminer"
                         :en "Fat-soluble Vitamins"}
         ::nutrient-ids ["Vit A" "Vit D" "Vit E"]}
        {:nutrient/id "Minerals"
         :nutrient/name {:nb "Mineraler"
                         :en "Minerals"}
         ::nutrient-ids ["Ca" "K" "Mg" "Na" "NaCl" "P"]}
        {:nutrient/id "TraceElements"
         :nutrient/name {:nb "Sporstoffer"
                         :en "Trace Elements"}
         ::nutrient-ids ["Fe" "I" "Cu" "Se" "Zn"]}]
       (map (juxt :nutrient/id identity))
       (into {})))

(def apriori-index
  (->> (vals apriori-groups)
       (mapcat #(map (fn [id] [id (:nutrient/id %)])
                     (::nutrient-ids %)))
       (into {})))

(defn get-parent
  "The FoodCase data currently does not group certain good groups that we want
  grouped, such as vitamins. This function provides a apriori parent while we
  wait for more structured source data."
  [id parent-id]
  (or (when (seq parent-id)
        {:nutrient/id parent-id})
      (when-let [parent-id (get apriori-index id)]
        {:nutrient/id parent-id})))

(defn get-apriori-groups []
  (map #(dissoc % ::nutrient-ids) (vals apriori-groups)))
