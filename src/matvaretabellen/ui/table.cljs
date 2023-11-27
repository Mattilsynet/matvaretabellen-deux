(ns matvaretabellen.ui.table
  (:require [matvaretabellen.ui.dom :as dom]
            [matvaretabellen.ui.food :as food]
            [matvaretabellen.ui.search :as search]))

(defn debounce [f timeout]
  (let [timer (atom nil)]
    (fn [& args]
      (some-> @timer js/clearTimeout)
      (reset! timer (js/setTimeout #(apply f args) timeout)))))

(defn get-column-id [el]
  (some-> el .-parentNode
          (.getAttribute "data-filter-id")))

(defn init-checkboxes [store filter-panel]
  (let [columns (:columns @store)]
    (doseq [input (dom/qsa filter-panel "input")]
      (if (columns (get-column-id input))
        (set! (.-checked input) true)
        (set! (.-checked input) false))))
  (->> (fn [e]
         (when-let [id (get-column-id (.-target e))]
           (js/setTimeout
            #(let [update-f (if (.-checked (.-target e)) conj disj)]
               (swap! store update :columns update-f id))
            ;; Give the checkbox transition time to complete before initiating
            ;; a somewhat heavy render. Without this delay, clicking the checkbox
            ;; will appear laggy
            250)))
       (.addEventListener filter-panel "input")))

(defn browse-foods [foods offset n]
  (let [max-n (count foods)]
    (cond-> {:current (->> foods
                           (drop offset)
                           (take n))}
      (< 0 offset)
      (assoc :previous [(Math/max 0 (- offset 250)) 250])

      (< (+ offset n) max-n)
      (assoc :next [(+ offset n) 250]))))

(defn filter-by-query [{:keys [foods idx]} locale e]
  (let [q (.-value (.-target e))]
    (if (<= 3 (.-length q))
      {:current (for [id (map :id (search/search @search/search-engine q locale))]
                  (get idx id))}
      (browse-foods foods 0 250))))

(defn init-filter-search [store form locale]
  (let [f (debounce #(->> (filter-by-query @store locale %)
                          (swap! store merge)) 250)]
    (.addEventListener (dom/qs form "input") "input" f)))

(defn render-food [el food columns lang]
  (doseq [td (.-childNodes el)]
    (let [id (.getAttribute td "data-id")]
      (if (columns id)
        (dom/show td)
        (dom/hide td))
      (case id
        "foodName"
        (let [a (.-firstChild td)]
          (set! (.-href a) (:url food))
          (set! (.-innerText a) (:foodName food)))

        "energyKj"
        (set! (.-innerText (dom/qs td ".mvt-num"))
              (.toLocaleString (:energyKj food) lang #js {:maximumFractionDigits 0}))

        "energyKcal"
        (set! (.-innerText td) (str (:energyKcal food) " kcal"))

        (let [el (.-firstChild td)
              decimals (some-> (.getAttribute el "data-decimals") parse-long)
              n (get-in food [:constituents id :quantity 0])]
          (set! (.-innerText el) (.toLocaleString n lang #js {:maximumFractionDigits (or decimals 1)}))
          (.setAttribute el "data-value" n)
          (.setAttribute el "data-portion" n))))))

(defn render-table [table {:keys [current columns]} template lang]
  (let [tbody (dom/qs table "tbody")
        rows (.-length (.-childNodes tbody))
        desired (count current)]
    (doseq [_i (range (- rows desired))]
      (.removeChild tbody (.-firstChild tbody)))
    (doseq [[i food] (map vector (range) current)]
      (let [el (or (aget (.-childNodes tbody) i)
                   (let [row (.cloneNode template true)]
                     (.appendChild tbody row)
                     row))]
        (render-food el food columns lang)))
    (doseq [th (dom/qsa table "thead th")]
      (if (columns (.getAttribute th "data-id"))
        (dom/show th)
        (dom/hide th)))
    (dom/re-zebra-table table)
    (dom/show table)))

(defn init-customizable-table [store lang table filter-panel]
  (let [rows (dom/qsa table "tbody tr")
        template (first rows)
        tbody (.-parentNode template)]
    (doall (map #(.removeChild tbody %) rows))
    (init-checkboxes store filter-panel)
    (add-watch
     store ::self
     (fn [_ _ old data]
       (render-table table data template lang)
       (when (not= (:columns old) (:columns data))
         (dom/set-local-edn "table-columns" (:columns data)))))))

(defn create-foods-store [data columns]
  (let [foods (->> (seq (.map (js/Object.values data) food/from-js))
                   (sort-by :foodName))]
    (atom {:foods foods
           :columns (set columns)
           :idx (into {} (map (juxt :id identity) foods))})))

(defn get-initial-table-columns [table]
  (->> (dom/qsa table "thead th")
       (remove #(dom/has-class % "mmm-hidden"))
       (map #(.getAttribute % "data-id"))
       set))

(defn get-initial-filter-columns [filter-panel]
  (->> (for [checkbox (dom/qsa filter-panel "input:checked")]
         (get-column-id checkbox))
       set))

(defn get-initial-columns [table filter-panel]
  (or (dom/get-local-edn "table-columns")
      (into (get-initial-table-columns table)
            (get-initial-filter-columns filter-panel))))

(defn init-giant-table [data filter-panel table locale]
  (let [store (create-foods-store data (get-initial-columns table filter-panel))]
    (init-customizable-table store (name locale) table filter-panel)
    (init-filter-search store (dom/qs ".mvt-filter-search") locale)
    (swap! store #(merge % (browse-foods (:foods %) 0 250)))))
