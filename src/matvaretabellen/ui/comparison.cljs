(ns matvaretabellen.ui.comparison
  (:require [clojure.string :as str]
            [matvaretabellen.diff :as diff]
            [matvaretabellen.food-name :as food-name]
            [matvaretabellen.ui.dom :as dom]
            [matvaretabellen.ui.food :as food]
            [matvaretabellen.ui.table :as table]
            [matvaretabellen.ui.tabs :as tabs]))

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

(defn get-abbreviated-name [food]
  (let [short (:shortName food)]
    (if (not= (:foodName food) short)
      (str "<abbr class=\"mmm-abbr\" title=\"" (:foodName food) "\">" short "</abbr>")
      short)))

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
     (set! (.-innerHTML el) (str "<a class=\"mmm-link\" href=\"" (:url food) "\">"
                                 (get-abbreviated-name food)
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
      (.remove (.-classList summary) "mmm-hidden"))))

(defn food->diffable [food]
  [(:id food) (update-vals (:constituents food) (comp first :quantity))])

(defn get-comparison-data [data ids]
  (->> ids
       (map #(food/from-js (aget data %)))
       with-short-names))

(defn initialize-share-button [button url]
  (->> (fn [e]
         (.preventDefault e)
         (js/navigator.clipboard.writeText (str js/window.location.origin url))
         (when-let [receipt (some-> button
                                    (.getAttribute "data-receipt")
                                    dom/qs)]
           (.remove (.-classList receipt) "mmm-hidden")
           (.add (.-classList receipt) "mmm-flash")
           (.removeChild (.-parentNode button) button)))
       (.addEventListener button "click")))

(defn init-rowwise-comparison [data foods locale table]
  (when table
    (let [store (table/init-components
                 data
                 locale
                 {:column-panel (js/document.getElementById "columns-panel")
                  :filter-panel (js/document.getElementById "food-group-panel")
                  :table table})]
      (swap! store #(assoc % ::table/current {:foods foods})))))

(defn init-share-buttons [params]
  (let [food-ids (get params "food-ids")
        url (str js/window.location.pathname "?food-ids=" food-ids)]
    (doseq [link (concat (dom/qsa (str "a[href='" js/window.location.pathname "']"))
                         (dom/qsa ".mvt-other-lang"))]
      (set! (.-href link) (str (.-href link) "?food-ids=" food-ids)))
    (doseq [share-button (dom/qsa ".mvtc-share")]
      (initialize-share-button share-button url))))

(defn get-notably-different-nutrients [foods]
  (let [statistics (some-> (dom/qs ".mvtc-statistics")
                           .-innerText
                           js/JSON.parse
                           js->clj)]
    (->> (map food->diffable foods)
         (diff/diff-constituents statistics)
         (diff/find-notable-diffs 0.5)
         keys
         set)))

(defn highlight-notable-differences [table notably-different]
  (doseq [id notably-different]
    (doseq [el (dom/qsa table (str "[data-nutrient-id='" id "']"))]
      (dom/add-class el "mmm-highlight"))))

(defn init-columnwise-comparison [foods table]
  ;; Highlight notably different cells first, so the template will be aptly
  ;; highlighted
  (highlight-notable-differences table (get-notably-different-nutrients foods))
  (doseq [row (dom/qsa table ".mvtc-comparison")]
    ;; Highlight absolutely compared values (energy numbers)
    (when-let [attr (some-> row (.getAttribute "data-compare-abs") keyword)]
      (let [ns (sort (map attr foods))]
        (when (< 1.5 (/ (last ns) (first ns)))
          (dom/add-class row "mmm-highlight"))))
    ;; Add placeholder columns for all foods
    (let [template (.-lastChild row)]
      (doseq [_ (next foods)]
        (.appendChild row (.cloneNode template true))))
    ;; Render data
    (doseq [[el food] (map vector (next (seq (.-childNodes row))) foods)]
      (prepare-comparison-el el food))))

(defn select-default-view [foods row-table _column-table]
  (when (< 3 (count foods))
    (-> (str "#" (.-id (.closest row-table ".mvtc-tab-target")))
        tabs/get-tab
        tabs/select-tab)))

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
    (let [selected? (some (comp #{(.getAttribute button "data-food-id")} :id) foods)]
      (cond
        (.contains (.-classList button) "mmm-button")
        (if selected?
          (.remove (.-classList button) "mmm-button-secondary")
          (.add (.-classList button) "mmm-button-secondary"))

        (.contains (.-classList button) "mmm-icon-button")
        (if selected?
          (.add (.-classList button) "mmm-icon-button-active")
          (.remove (.-classList button) "mmm-icon-button-active"))))))

(defn get-pill-template [pills]
  (when-not (aget pills "template")
    (aset pills "template" (.-firstChild pills)))
  (aget pills "template"))

(defn animate-height [el from to & [callback]]
  (.add (.-classList el) "mmm-init-height-transition")
  (set! (.-height (.-style el)) from)
  (js/requestAnimationFrame
   (fn []
     (.remove (.-classList el) "mmm-init-height-transition")
     (.add (.-classList el) "mmm-height-transition")
     (->> (fn cleanup []
            (set! (.-height (.-style el)) nil)
            (.remove (.-classList el) "mmm-height-transition")
            (when (ifn? callback)
              (callback))
            (.removeEventListener el "transitionend" cleanup))
          (.addEventListener el "transitionend"))
     (js/requestAnimationFrame #(set! (.-height (.-style el)) to)))))

(defn get-height [el]
  (.-height (.getBoundingClientRect el)))

(defn open-drawer [drawer {:keys [animate?]}]
  (when (.contains (.-classList drawer) "mmm-drawer-closed")
    (.remove (.-classList drawer) "mmm-drawer-closed")
    (when animate?
      (animate-height drawer "0" (str (get-height drawer) "px")))))

(defn close-drawer [drawer {:keys [animate?]}]
  (when-not (.contains (.-classList drawer) "mmm-drawer-closed")
    (if-not animate?
      (.add (.-classList drawer) "mmm-drawer-closed")
      (animate-height drawer (str (get-height drawer) "px") "0" #(.add (.-classList drawer) "mmm-drawer-closed")))))

(defn get-suggestions []
  (for [el (dom/qsa "[data-comparison-suggestion-id]")]
    {:id (.getAttribute el "data-comparison-suggestion-id")
     :foodName (.getAttribute el "data-comparison-suggestion-name")}))

(defn update-suggestions [el foods suggestions]
  (if-not (seq suggestions)
    (dom/hide el)
    (let [list (dom/qs el "ul")
          template (.-firstChild list)]
      (dom/show el)
      (set! (.-innerHTML list) "")
      (doseq [suggestion suggestions]
        (let [li (.cloneNode template true)]
          (set! (.-innerHTML (.-firstChild li)) (:foodName suggestion))
          (->> (fn [_e]
                 (->> (conj (get-foods-to-compare) suggestion)
                      (update-food-store foods)))
               (.addEventListener li "click"))
          (.appendChild list li))))))

(defn update-drawer [foods selector opt]
  (when-let [drawer (js/document.querySelector selector)]
    (let [pills (.querySelector drawer ".mmm-pills")
          template (get-pill-template pills)
          button (.querySelector drawer ".mmm-button")]
      (if (< 0 (count @foods))
        (open-drawer drawer opt)
        (close-drawer drawer opt))
      (set! (.-href button) (str (first (str/split (.-href button) #"\?"))
                                 "?food-ids=" (str/join "," (map :id @foods))))
      (set! (.-innerHTML pills) "")
      (doseq [food @foods]
        (let [pill (.cloneNode template true)]
          (set! (.-innerHTML (.querySelector pill ".mvtc-food-name")) (get-abbreviated-name food))
          (.addEventListener pill "click" (fn [_e]
                                            (->> (get-foods-to-compare)
                                                 (remove #(= (:id food) (:id %)))
                                                 (update-food-store foods))))
          (.appendChild pills pill)))
      (->> (get-suggestions)
           (remove (comp (set (map :id @foods)) :id))
           (update-suggestions (.querySelector drawer ".mvtc-suggestions") foods)))))

(defn get-food-data [el]
  (when-let [id (some-> el (.getAttribute "data-food-id"))]
    {:id id
     :foodName (.getAttribute el "data-food-name")}))

(defn update-comparison-uis [foods buttons-selector drawer-selector & [opt]]
  (update-buttons @foods buttons-selector)
  (update-drawer foods drawer-selector opt))

(defn toggle-comparison [foods data]
  (let [updated (if (some (comp #{(:id data)} :id) @foods)
                  (remove #(= (:id data) (:id %)) @foods)
                  (concat @foods [data]))]
    (update-food-store foods updated)))

(defn initialize-drawer [drawer foods]
  (->> (fn [_e]
         (js/requestAnimationFrame #(update-food-store foods nil)))
       (.addEventListener (.querySelector drawer ".mmm-icon-button") "click"))
  (when-let [page-food (get-food-data (dom/qs "#food-data"))]
    (->> (fn [_e]
           (when-not ((set (map :id @foods)) (:id page-food))
             (update-food-store foods (conj @foods page-food))))
         (.addEventListener (dom/qs drawer ".mmm-button") "click"))))

(defn initialize-tooling
  "Initialize the compare button and the comparison drawer on pages that are not
  the comparison page."
  [buttons-selector drawer-selector]
  (let [foods (atom (get-foods-to-compare))]
    (->> (fn [_ _ _ new-foods]
           (stage-comparisons new-foods)
           (update-comparison-uis foods buttons-selector drawer-selector {:animate? true}))
         (add-watch foods ::director))
    (when (< 0 (count @foods))
      (some-> (dom/qs drawer-selector) (initialize-drawer foods)))
    (update-comparison-uis foods buttons-selector drawer-selector)
    (doseq [button (dom/qsa buttons-selector)]
      (.remove (.-classList button) "mmm-hidden")
      (->> (fn [_e]
             (toggle-comparison foods (get-food-data button)))
           (.addEventListener button "click")))))
