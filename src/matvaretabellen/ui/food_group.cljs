(ns matvaretabellen.ui.food-group
  (:require [matvaretabellen.ui.dom :as dom]))

(defn get-local-storage-k []
  (str "food-groups" (hash js/window.location.pathname)))

(defn get-filter-store []
  (atom (set (dom/get-local-edn (get-local-storage-k)))))

(defn sync-filters [filters]
  (dom/set-local-edn (get-local-storage-k) filters))

(defn set-checked [panel id checked?]
  (doseq [checkbox (dom/qsa panel (str "label[data-food-group-id='" id "'] input"))]
    (set! (.-checked checkbox) checked?)))

(defn hide-lists [panel id]
  (doseq [ul (-> (dom/qs panel (str "label[data-food-group-id='" id "']"))
                 (.closest "li")
                 (dom/qsa "ul"))]
    (.add (.-classList ul) "mmm-hidden")))

(defn get-rows [table id]
  (dom/qsa table (str "tr[data-food-group-id" (when id (str "='" id "'")) "]")))

(defn hide-rows [table & [id]]
  (doseq [tr (get-rows table id)]
    (.add (.-classList tr) "mmm-hidden")))

(defn show-rows [table & [id]]
  (doseq [tr (get-rows table id)]
    (.remove (.-classList tr) "mmm-hidden")))

(defn re-zebra-table [table]
  (.remove (.-classList table) "mmm-table-zebra")
  (doseq [[i tr] (->> (dom/qsa table "tbody tr")
                      (remove #(.contains (.-classList %) "mmm-hidden"))
                      (map vector (range)))]
    (if (= 0 (mod i 2))
      (.add (.-classList tr) "mmm-zebra-strip")
      (.remove (.-classList tr) "mmm-zebra-strip"))))

(defn update-ui [panel table previous filters]
  (when (empty? previous)
    (hide-rows table))
  (doseq [unchecked-id (remove filters previous)]
    (set-checked panel unchecked-id false)
    (hide-lists panel unchecked-id)
    (hide-rows table unchecked-id))
  (doseq [id filters]
    (doseq [checkbox (dom/qsa panel (str "label[data-food-group-id='" id "'] input"))]
      (set! (.-checked checkbox) true)
      (.remove (.-classList (.closest checkbox "ul")) "mmm-hidden"))
    (show-rows table id))
  (when (empty? filters)
    (show-rows table))
  (re-zebra-table table))

(defn get-filter-id [checkbox]
  (some-> checkbox .-parentNode (.getAttribute "data-food-group-id")))

(defn get-checked-children [li]
  (->> (.-childNodes li)
       (filter #(= "UL" (.-tagName %)))
       (mapcat #(.-childNodes %))
       (mapcat #(.-childNodes %))
       (filter #(= "LABEL" (.-tagName %)))
       (map #(.-firstChild %))
       (filter #(.-checked %))))

(defn get-empty-parent-ids [li]
  (when-let [label (some->> li
                            .-childNodes
                            (filter #(= "LABEL" (.-tagName %)))
                            first)]
    (when-not (.-checked (dom/qsa label "input"))
      (cond->> (get-empty-parent-ids (.closest (.-parentNode li) "li"))
        (= 0 (count (get-checked-children li)))
        (cons (.getAttribute label "data-food-group-id"))))))

(defn get-filter-actions [el]
  (when (= "checkbox" (some-> el (.getAttribute "type")))
    (let [container (.closest el "li")
          pids (get-empty-parent-ids container)]
      {:enable? (.-checked el)
       :ids (concat
             (->> (dom/qsa container "li input[type=checkbox]")
                  (map get-filter-id))
             [(get-filter-id el)]
             pids)})))

(defn toggle-filter [e store]
  (when-let [{:keys [enable? ids]} (get-filter-actions (.-target e))]
    (swap! store (fn [filters]
                   (if enable?
                     (into filters ids)
                     (reduce disj filters ids))))))

(defn initialize-filter [panel table]
  (let [store (get-filter-store)]
    (add-watch store ::filters (fn [_ _ previous filters]
                                 (sync-filters filters)
                                 (update-ui panel table previous filters)))
    (.addEventListener js/document.body "click" #(toggle-filter % store))
    (when (seq @store)
      (update-ui panel table nil @store))))
