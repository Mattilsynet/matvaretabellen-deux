(ns matvaretabellen.ui.comparison
  (:require [clojure.string :as str]
            [matvaretabellen.diff :as diff]
            [matvaretabellen.urls :as urls]))

(defn qsa [selector]
  (seq (js/document.querySelectorAll selector)))

(defn qs [selector]
  (js/document.querySelector selector))

(def comparison-k "comparisonFoods")

(defn keywordize-some
  "Keyborizes most keys except for contituent ids, which are strings with
  keyword-unfriendly characters"
  [food]
  (-> food
      (update-keys keyword)
      (update :constituents
              (fn [constituents]
                (->> (for [[nutrient-id x] constituents]
                       [nutrient-id (update-keys x keyword)])
                     (into {}))))))

(defn get-foods-to-compare []
  (some->> (js/localStorage.getItem comparison-k)
           not-empty
           js/JSON.parse
           js->clj
           (map keywordize-some)))

(def name-length 20)

(defn get-short-name [food]
  (if (< (count (:foodName food)) name-length)
    (:foodName food)
    (let [pieces (str/split (:foodName food) #",")
          candidate (first pieces)]
      (if (< (count candidate) name-length)
        candidate
        (loop [words (seq (str/split candidate #" "))
               res []]
          (if (nil? words)
            (str/join " " res)
            (let [word (first words)
                  new-res (conj res word)]
              (if (< (reduce + (map count new-res)) name-length)
                (recur (next words) new-res)
                (str/join " " res)))))))))

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
                                 (let [short (get-short-name food)]
                                   (if (not= (:foodName food) short)
                                     (str "<abbr class=\"mmm-abbr\" title=\"" (:foodName food) "\">" short "</abbr>")
                                     short))
                                 "</a>")))

   (when (.contains (.-classList el) "mvtc-energy")
     (set-energy el food))

   (when-let [edible (.querySelector el ".mvtc-edible-part")]
     (set! (.-innerHTML edible) (or (:ediblePart food) "0")))

   (when (.contains (.-classList el) "mvtc-nutrient")
     (set-nutrient-content el food))))

(defn get-energy-rating-text [id->energy]
  (let [rating (->> id->energy
                    (sort-by (comp - second))
                    diff/rate-energy-diff
                    (sort-by (comp - diff/get-rating-severity :rating))
                    first
                    :rating)]
    (some-> (str "[data-rating=" (name rating) "]") qs .-innerText)))

(defn enumerate [xs]
  (if (< 1 (count xs))
    (if-let [and (some-> "[data-k=and]" qs .-innerText)]
      (str (str/join ", " (butlast xs)) " " and " " (last xs))
      (str/join ", " xs))
    (str/join xs)))

(defn update-summary [foods]
  (let [id->energy (map (juxt :id :energyKj) foods)
        equivalents (diff/get-energy-equivalents id->energy)
        summary (qs ".mvtc-rating-summary")]
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
  (for [id ids]
    (keywordize-some (js->clj (aget data id)))))

(defn initialize-page
  "Initialize the comparison page"
  [data params]
  (when-let [foods (->> (str/split (get params "food-ids") ",")
                        (get-comparison-data data))]
    (when (< 4 (count foods))
      (let [container (js/document.getElementById "container")]
        (.remove (.-classList container) "mmm-container-focused")
        (.add (.-classList container) "mmm-container")))
    (update-summary foods)
    (let [statistics (some-> (qs ".mvtc-statistics")
                             .-innerText
                             js/JSON.parse
                             js->clj)
          notably-different (->> (map food->diffable foods)
                                 (diff/diff-constituents statistics)
                                 (diff/find-notable-diffs 0.5)
                                 keys
                                 set)]
      (doseq [row (qsa ".mvtc-comparison")]
        (when (notably-different (.getAttribute row "data-nutrient-id"))
          (.add (.-classList row) "mmm-highlight"))
        (let [template (.-lastChild row)]
          (doseq [_food (next foods)]
            (.appendChild row (.cloneNode template true))))
        (doseq [[el food] (map vector (next (seq (.-childNodes row))) foods)]
          (prepare-comparison-el el food))))))

;; Comparison UI on other pages

(defn stage-comparisons [foods]
  (->> foods
       clj->js
       js/JSON.stringify
       (js/localStorage.setItem comparison-k)))

(defn update-buttons [foods data selector]
  (let [comparing? (some (comp #{(:id data)} :id) foods)]
    (doseq [button (qsa selector)]
      (if comparing?
        (.remove (.-classList button) "mmm-button-secondary")
        (.add (.-classList button) "mmm-button-secondary")))))

(defn get-pill-template [pills]
  (when-not (aget pills "template")
    (aset pills "template" (.-firstChild pills)))
  (aget pills "template"))

(defn update-drawer [foods selector]
  (when-let [drawer (js/document.querySelector selector)]
    (let [pills (.querySelector drawer ".mmm-pills")
          template (get-pill-template pills)]
      (if (< 0 (count @foods))
        (.remove (.-classList drawer) "mmm-drawer-closed")
        (.add (.-classList drawer) "mmm-drawer-closed"))
      (if (< 1 (count @foods))
        (.remove (.-classList (.querySelector drawer ".mmm-button")) "mmm-button-disabled")
        (.add (.-classList (.querySelector drawer ".mmm-button")) "mmm-button-disabled"))
      (set! (.-innerHTML pills) "")
      (doseq [food @foods]
        (let [pill (.cloneNode template true)]
          (set! (.-innerHTML (.querySelector pill ".mvtc-food-name")) (:foodName food))
          (.addEventListener pill "click" (fn [_e]
                                            (->> (get-foods-to-compare)
                                                 (remove #(= (:id food) (:id %)))
                                                 (reset! foods))))
          (.appendChild pills pill))))))

(defn update-comparison-uis [foods data buttons-selector drawer-selector]
  (update-buttons @foods data buttons-selector)
  (update-drawer foods drawer-selector))

(defn toggle-comparison [foods data buttons-selector drawer-selector]
  (let [updated (if (some (comp #{(:id data)} :id) @foods)
                  (remove #(= (:id data) (:id %)) @foods)
                  (concat @foods [data]))]
    (reset! foods updated)
    (update-comparison-uis foods data buttons-selector drawer-selector)))

(defn initialize-tooling
  "Initialize the compare button and the comparison drawer on pages that are not
  the comparison page."
  [buttons-selector drawer-selector]
  (let [data (some-> (js/document.getElementById "data")
                     .-innerText
                     js/JSON.parse
                     (js->clj :keywordize-keys true))
        foods (atom (get-foods-to-compare))]
    (add-watch foods ::director (fn [_ _ _ new-foods]
                                  (stage-comparisons new-foods)
                                  (update-comparison-uis foods data buttons-selector drawer-selector)))
    (when (< 0 (count @foods))
      (when-let [drawer (js/document.querySelector drawer-selector)]
        (set! (.-transition (.-style drawer)) "none")))
    (update-comparison-uis foods data buttons-selector drawer-selector)
    (doseq [button (qsa buttons-selector)]
      (.remove (.-classList button) "mmm-hidden")
      (->> (fn [_e]
             (toggle-comparison foods data buttons-selector drawer-selector))
           (.addEventListener button "click")))))
