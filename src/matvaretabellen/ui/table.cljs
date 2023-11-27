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

(def page-size 250)

(defn browse-foods [foods offset n]
  (let [max-n (count foods)]
    {:current (->> foods
                   (drop offset)
                   (take n))
     :prev (when (< 0 offset)
             [::browse-foods (Math/max 0 (- offset page-size)) page-size])
     :next (when (< (+ offset n) max-n)
             [::browse-foods (+ offset n) page-size])}))

(defn filter-by-query [{:keys [foods idx]} locale e]
  (let [q (.-value (.-target e))]
    (if (<= 3 (.-length q))
      {:current (for [id (map :id (search/search @search/search-engine q locale))]
                  (get idx id))
       :next nil
       :prev nil}
      (browse-foods foods 0 page-size))))

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
        (when (:energyKj food)
          (set! (.-innerText (dom/qs td ".mvt-num"))
                (.toLocaleString (:energyKj food) lang #js {:maximumFractionDigits 0})))

        "energyKcal"
        (set! (.-innerText td) (str (:energyKcal food) " kcal"))

        (let [el (.-firstChild td)
              decimals (some-> (.getAttribute el "data-decimals") parse-long)
              n (get-in food [:constituents id :quantity 0])]
          (set! (.-innerText el) (.toLocaleString n lang #js {:maximumFractionDigits (or decimals 1)}))
          (.setAttribute el "data-value" n)
          (.setAttribute el "data-portion" n))))))

(defn update-button [button action]
  (when button
    (if action
      (dom/show button)
      (dom/hide button))))

(defn render-table [table {:keys [current columns prev next sort-by]} template lang]
  (let [tbody (dom/qs table "tbody")
        rows (.-length (.-childNodes tbody))
        desired (count current)
        [sort-id sort-order] sort-by
        container (.-parentNode table)]
    (doseq [_i (range (- rows desired))]
      (.removeChild tbody (.-firstChild tbody)))
    (doseq [[i food] (map vector (range) current)]
      (let [el (or (aget (.-childNodes tbody) i)
                   (let [row (.cloneNode template true)]
                     (.appendChild tbody row)
                     row))]
        (render-food el food columns lang)))
    (doseq [th (dom/qsa table "thead th")]
      (let [id (.getAttribute th "data-id")]
        (if (columns (.getAttribute th "data-id"))
          (dom/show th)
          (dom/hide th))
        (let [icon (dom/qs th ".mvt-sort-icon")
              selector (if (= sort-id id)
                         (if (= sort-order :sort.order/asc)
                           ".mvt-asc"
                           ".mvt-desc")
                         ".mvt-sort")]
          (set! (.-innerHTML icon) "")
          (.appendChild icon (.cloneNode (dom/qs container selector) true)))))
    (dom/re-zebra-table table)
    (dom/show table)
    (update-button (dom/qs container ".mvt-prev") prev)
    (update-button (dom/qs container ".mvt-next") next)))

(defn dispatch-action [store action]
  (let [[action & args] action]
    (case action
      ::browse-foods (swap! store #(merge % (apply browse-foods (:foods %) args))))))

(defn init-button [button store k]
  (when button
    (->> (fn [e]
           (.preventDefault e)
           (dispatch-action store (k @store)))
         (.addEventListener button "click"))))

(defn get-sort-f [id]
  (case id
    "foodName" :foodName
    "energyKj" :energyKj
    "energyKcal" :energyKcal
    #(get-in % [:constituents id :quantity])))

(defn change-sort [store e]
  (when-let [th (.closest (.-target e) "th")]
    (swap!
     store
     (fn [data]
       (let [id (.getAttribute th "data-id")
             [curr-id curr-dir] (:sort-by data)
             dir (if (and (= curr-id id)
                          (= curr-dir :sort.order/desc))
                   :sort.order/asc
                   :sort.order/desc)
             sf (get-sort-f (.getAttribute th "data-id"))]
         (merge data
                {:current (cond-> (sort-by sf (:current data))
                            (= dir :sort.order/desc) reverse)
                 :sort-by [id dir]}))))))

(defn init-customizable-table [store lang table filter-panel]
  (let [rows (dom/qsa table "tbody tr")
        template (first rows)
        tbody (.-parentNode template)]
    (doall (map #(.removeChild tbody %) rows))
    (init-checkboxes store filter-panel)
    (init-button (dom/qs (.-parentNode table) ".mvt-prev") store :prev)
    (init-button (dom/qs (.-parentNode table) ".mvt-next") store :next)
    (.addEventListener (dom/qs table "thead") "click" #(change-sort store %))
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
    (swap! store #(merge % (browse-foods (:foods %) 0 page-size)))))
