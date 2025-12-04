(ns matvaretabellen.ui.comparison
  (:require [clojure.string :as str]
            [matvaretabellen.diff :as diff]
            [matvaretabellen.food-name :as food-name]
            [matvaretabellen.ui.dom :as dom]
            [matvaretabellen.ui.food :as food]
            [matvaretabellen.ui.table :as table]
            [matvaretabellen.ui.tabs :as tabs]
            [matvaretabellen.ui.toggler :as toggler]))

(defn with-short-names [foods]
  (map (fn [food name]
         (assoc food :shortName name))
       foods
       (food-name/shorten-names (map :foodName foods))))

(defn update-food-store [store foods]
  (reset! store (with-short-names foods)))

(def comparison-k "comparisonFoods")

(defn get-foods-to-compare []
  (some->> (js/localStorage.getItem comparison-k)
           not-empty
           js/JSON.parse
           (map food/from-js)))

(defn set-energy [el food]
  (when-let [kj (.querySelector el ".mvt-kj")]
    (set! (.-innerHTML kj) (:energyKj food))
    (.setAttribute kj "data-portion" (:energyKj food)))
  (when-let [kcal (.querySelector el ".mvt-kcal")]
    (set! (.-innerHTML kcal) (:energyKcal food))
    (.setAttribute kcal "data-portion" (:energyKcal food))))

(defn set-nutrient-content [el food]
  (let [[n sym] (some-> (:constituents food)
                        (get (.getAttribute el "data-nutrient-id"))
                        :quantity)
        num-el (.querySelector el "[data-portion]")]
    (set! (.-innerHTML num-el) n)
    (.setAttribute num-el "data-portion" n)
    (set! (.-innerHTML (.querySelector el ".mvt-sym")) sym)))

(defn prepare-comparison-el [el food]
  (or
   (when (.contains (.-classList el) "mvtc-food-name")
     (set! (.-innerHTML el) (str "<a href=\"" (:url food) "\"" (when-not (= (:shortName food) (:foodName food))
                                                                 (str " data-tooltip=\"" (:foodName food) "\"")) ">"
                                 (:shortName food)
                                 "</a>")))

   (when (.contains (.-classList el) "mvtc-energy")
     (set-energy el food))

   (when-let [edible (.querySelector el ".mvtc-edible-part")]
     (set! (.-innerHTML edible) (or (:ediblePart food) "0")))

   (when (.contains (.-classList el) "mvtc-nutrient")
     (set-nutrient-content el food))))

(defn get-energy-rating-text [id->energy]
  (when-let [rating (->> id->energy
                         (sort-by (comp - second))
                         diff/rate-energy-diff
                         (sort-by (comp - diff/get-rating-severity :rating))
                         first
                         :rating)]
    (some-> (str "[data-rating=" (name rating) "]") dom/qs .-innerText)))

(defn enumerate [xs]
  (if (< 1 (count xs))
    (if-let [and (some-> "[data-k=and]" dom/qs .-innerText)]
      (str (str/join ", " (butlast xs)) " " and " " (last xs))
      (str/join ", " xs))
    (str/join xs)))

(defn update-summary
  "Generate a neat little comparison summary of the foods. Sadly not currently in
  use."
  [foods]
  (let [id->energy (map (juxt :id :energyKj) foods)
        equivalents (diff/get-energy-equivalents id->energy)
        summary (dom/qs ".mvtc-rating-summary")]
    (when-let [rating-text (get-energy-rating-text id->energy)]
      (let [text (-> (.-innerHTML summary)
                     (.replace "${rating}" rating-text)
                     (.replace "${reference}" (str "100g " (:foodName (first foods))))
                     (.replace "${comparisons}" (->> (map (fn [food equiv]
                                                            (str (.toFixed (* 100 (:amount equiv)) 1) "g " (:foodName food)))
                                                          (rest foods)
                                                          equivalents)
                                                     enumerate)))]
        (set! (.-innerHTML summary) text))
      (dom/show summary))))

(defn food->diffable [food]
  [(:id food) (update-vals (:constituents food) (comp first :quantity))])

(defn get-comparison-data [data ids]
  (->> ids
       (map #(food/from-js (aget data %)))
       with-short-names))

(defn initialize-share-button [button url]
  (->> (fn [e]
         (.preventDefault e)
         (js/navigator.clipboard.writeText (str js/window.location.origin url)))
       (.addEventListener button "click")))

(defn init-rowwise-comparison [data foods locale table]
  (when table
    (let [store (table/init-components
                 data
                 locale
                 {:column-panel (js/document.getElementById "columns-panel")
                  :filter-panel (js/document.getElementById "food-group-panel")
                  :download-buttons (dom/qsa ".mvt-download")
                  :table table})]
      (swap! store #(-> %
                        (assoc ::table/current {:foods foods})
                        (assoc ::table/selected (map :id foods)))))))

(defn init-share-buttons [params]
  (let [food-ids (get params "food-ids")
        url (str js/window.location.pathname "?food-ids=" food-ids)]
    (doseq [link (concat (dom/qsa (str "a[href='" js/window.location.pathname "']"))
                         (dom/qsa ".mvt-other-lang"))]
      (set! (.-href link) (str (.-href link) "?food-ids=" food-ids)))
    (doseq [share-button (dom/qsa ".mvtc-share")]
      (initialize-share-button share-button url))))

(defn init-columnwise-comparison [foods table]
  ;; Highlight notably different cells first, so the template will be aptly
  ;; highlighted
  ;; (highlight-notable-differences table (get-notably-different-nutrients foods))
  ;; Highlights where not well understood by the designers, so disabled for now.
  (doseq [row (dom/qsa table ".mvtc-comparison")]
    ;; Add placeholder columns for all foods
    (let [template (.-lastChild row)]
      (doseq [_ (next foods)]
        (.appendChild row (.cloneNode template true))))
    ;; Render data
    (doseq [[el food] (map vector (next (seq (.-childNodes row))) foods)]
      (prepare-comparison-el el food))))

(defn select-default-view [foods row-table _column-table]
  (when (< 3 (count foods))
    (let [tab (tabs/get-tab (str "#" (.-id (.closest row-table ".mvtc-tab-target"))))]
      (when (dom/visible? tab)
        (tabs/select-tab tab)))))

(defn initialize-page
  "Initialize the comparison page"
  [data locale params]
  (when-let [foods (->> (str/split (get params "food-ids") ",")
                        (get-comparison-data data))]
    (let [column-table (js/document.getElementById "columnwise-table")
          row-table (js/document.getElementById "rowwise-table")]
      (init-columnwise-comparison foods column-table)
      (init-rowwise-comparison data foods locale row-table)
      (init-share-buttons params)
      (select-default-view foods row-table column-table))))

;; Comparison UI on other pages

(defn stage-comparisons [foods]
  (->> foods
       clj->js
       js/JSON.stringify
       (js/localStorage.setItem comparison-k)))

(defn update-buttons [foods selector]
  (doseq [button (dom/qsa selector)]
    (when-not (dom/has-attr? button "data-original-variant")
      (dom/set-attr button "data-original-variant" (dom/get-attr button "data-variant")))
    (dom/set-attr button "data-variant"
                  (if (some (comp #{(.getAttribute button "data-food-id")} :id) foods)
                    "primary"
                    (dom/get-attr button "data-original-variant")))))

(defn get-pill-template [pills]
  (when-not (aget pills "template")
    (aset pills "template" (.-firstChild pills)))
  (aget pills "template"))

(defn open-drawer [^js drawer]
  (.showModal drawer))

(defn close-drawer [^js drawer]
  (.close drawer))

(defn get-suggestions []
  (for [el (dom/qsa "[data-comparison-suggestion-id]")]
    {:id (.getAttribute el "data-comparison-suggestion-id")
     :foodName (.getAttribute el "data-comparison-suggestion-name")
     :shortName (.-textContent el)}))

(defn update-suggestions [el foods suggestions]
  (if-not (seq suggestions)
    (dom/hide el)
    (let [list (dom/qs el "ul")
          template (.-firstChild list)]
      (dom/show el)
      (set! (.-innerHTML list) "")
      (doseq [suggestion suggestions]
        (let [li (.cloneNode template true)]
          (set! (.-innerHTML (.-firstChild li)) (:shortName suggestion))
          (when-not (= (:shortName suggestion) (:foodName suggestion))
            (.setAttribute (.-firstChild li) "data-tooltip" (:foodName suggestion)))
          (->> (fn [_e]
                 (->> (conj (get-foods-to-compare) suggestion)
                      (update-food-store foods)))
               (.addEventListener li "click"))
          (.appendChild list li))))))

(defn update-drawer [foods selector]
  (when-let [drawer (js/document.querySelector selector)]
    (let [pills (.querySelector drawer ".mvtc-drawer-foods")
          template (get-pill-template pills)
          button (.querySelector drawer ".mvtc-drawer-compare")]
      (set! (.-href button) (str (first (str/split (.-href button) #"\?"))
                                 "?food-ids=" (str/join "," (map :id @foods))))
      (set! (.-innerHTML pills) "")
      (doseq [food @foods]
        (let [pill (.cloneNode template true)]
          (set! (.-innerHTML (.querySelector pill ".mvtc-food-name")) (:shortName food))
          (when-not (= (:shortName food) (:foodName food))
            (.setAttribute pill "data-tooltip" (:foodName food)))
          (.addEventListener pill "click" (fn [_e]
                                            (->> (get-foods-to-compare)
                                                 (remove #(= (:id food) (:id %)))
                                                 (update-food-store foods))))
          (.appendChild pills pill)))
      (->> (get-suggestions)
           (remove (comp (set (map :id @foods)) :id))
           (update-suggestions (.querySelector drawer ".mvtc-suggestions") foods))
      (if (< 0 (count @foods))
        (open-drawer drawer)
        (close-drawer drawer)))))

(defn get-food-data [el]
  (when-let [id (some-> el (.getAttribute "data-food-id"))]
    {:id id
     :foodName (.getAttribute el "data-food-name")}))

(defn update-comparison-uis [foods buttons-selector drawer-selector]
  (update-buttons @foods buttons-selector)
  (update-drawer foods drawer-selector))

(defn toggle-comparison [foods data]
  (let [updated (if (some (comp #{(:id data)} :id) @foods)
                  (remove #(= (:id data) (:id %)) @foods)
                  (concat @foods [data]))]
    (update-food-store foods updated)))

(defn initialize-drawer [drawer foods]
  (->> (fn [_e]
         (update-food-store foods nil))
       (.addEventListener (.querySelector drawer ".mvtc-drawer-close") "click"))
  (when-let [page-food (get-food-data (dom/qs "#food-data"))]
    (->> (fn [_e]
           (when-not ((set (map :id @foods)) (:id page-food))
             (update-food-store foods (conj @foods page-food))))
         (.addEventListener (dom/qs drawer ".mvtc-drawer-compare") "click"))))

(defn initialize-tooling
  "Initialize the compare button and the comparison drawer on pages that are not
  the comparison page."
  [buttons-selector drawer-selector]
  (let [foods (atom (get-foods-to-compare))]
    (->> (fn [_ _ _ new-foods]
           (stage-comparisons new-foods)
           (update-comparison-uis foods buttons-selector drawer-selector))
         (add-watch foods ::director))
    (some-> (dom/qs drawer-selector) (initialize-drawer foods))
    (update-comparison-uis foods buttons-selector drawer-selector)
    (doseq [button (dom/qsa buttons-selector)]
      (dom/show button)
      (->> (fn [_e]
             (toggle-comparison foods (get-food-data button)))
           (.addEventListener button "click")))))
