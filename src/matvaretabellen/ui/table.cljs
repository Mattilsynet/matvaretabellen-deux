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

(defn browse-foods [{:keys [foods page-size current]} offset]
  (let [max-n (count foods)]
    {:foods foods
     :offset offset
     :sort-by (or (:sort-by current) [:foodName :sort.order/asc])
     :n page-size
     :prev (when (< 0 offset)
             [::browse-foods (Math/max 0 (- offset page-size))])
     :next (when (< (+ offset page-size) max-n)
             [::browse-foods (+ offset page-size)])}))

(defn filter-by-query [{:keys [foods idx]} locale e]
  (let [q (.-value (.-target e))]
    (if (<= 3 (.-length q))
      {:foods (for [id (map :id (search/search @search/search-engine q locale))]
                (get idx id))}
      (browse-foods foods 0))))

(defn init-filter-search [store form locale]
  (let [f (debounce #(->> (filter-by-query @store locale %)
                          (swap! store assoc :current)) 250)]
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

(defn get-sort-f [id]
  (case id
    "foodName" :foodName
    "energyKj" :energyKj
    "energyKcal" :energyKcal
    #(get-in % [:constituents id :quantity])))

(defn sort-foods [[id dir] foods]
  (cond-> (sort-by (get-sort-f id) foods)
    (= dir :sort.order/desc) reverse))

(defn get-current-foods [{:keys [foods offset n sort-by]}]
  (cond->> foods
    sort-by (sort-foods sort-by)
    offset (drop offset)
    n (take n)))

(defn render-rows [tbody template {:keys [current columns lang]}]
  (let [rows (.-length (.-childNodes tbody))
        foods (get-current-foods current)
        desired (count foods)]
    (doseq [_i (range (- rows desired))]
      (.removeChild tbody (.-firstChild tbody)))
    (doseq [[i food] (map vector (range) foods)]
      (let [el (or (aget (.-childNodes tbody) i)
                   (let [row (.cloneNode template true)]
                     (.appendChild tbody row)
                     row))]
        (render-food el food columns lang)))))

(defn render-table [table template {:keys [columns current] :as data}]
  (let [[sort-id sort-order] (:sort-by current)
        container (.-parentNode table)]
    (render-rows (dom/qs table "tbody") template data)
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
    (update-button (dom/qs container ".mvt-prev") (:prev current))
    (update-button (dom/qs container ".mvt-next") (:next current))))

(defn dispatch-action [store action]
  (let [[action & args] action]
    (case action
      ::browse-foods (swap! store #(assoc % :current (apply browse-foods % args))))))

(defn init-button [button store k]
  (when button
    (->> (fn [e]
           (.preventDefault e)
           (when-let [action (k (:current @store))]
             (dispatch-action store action)))
         (.addEventListener button "click"))))

(defn change-sort [store e]
  (when-let [th (.closest (.-target e) "th")]
    (swap!
     store
     (fn [data]
       (let [id (.getAttribute th "data-id")
             [curr-id curr-dir] (-> data :current :sort-by)
             dir (if (and (= curr-id id)
                          (= curr-dir :sort.order/desc))
                   :sort.order/asc
                   :sort.order/desc)]
         (assoc-in data [:current :sort-by] [id dir]))))))

(defn init-customizable-table [store table filter-panel]
  (let [rows (dom/qsa table "tbody tr")
        template (first rows)
        tbody (.-parentNode template)]
    (doall (map #(.removeChild tbody %) rows))
    (init-checkboxes store filter-panel)
    (init-button (dom/qs (.-parentNode table) ".mvt-prev") store :prev)
    (init-button (dom/qs (.-parentNode table) ".mvt-next") store :next)
    (->> #(change-sort store %)
         (.addEventListener (dom/qs table "thead") "click"))
    (add-watch
     store ::self
     (fn [_ _ old data]
       (render-table table template data)
       (when (not= (:columns old) (:columns data))
         (dom/set-local-edn "table-columns" (:columns data)))))))

(defn create-foods-store [data {:keys [columns page-size]} lang]
  (let [foods (->> (seq (.map (js/Object.values data) food/from-js))
                   (sort-by :foodName))]
    (atom {:foods foods
           :columns (set columns)
           :page-size (or page-size 250)
           :idx (into {} (map (juxt :id identity) foods))
           :lang lang})))

(defn get-initial-table-columns [table]
  (->> (dom/qsa table "thead th")
       (remove #(dom/has-class % "mmm-hidden"))
       (map #(.getAttribute % "data-id"))
       set))

(defn get-initial-filter-columns [filter-panel]
  (->> (for [checkbox (dom/qsa filter-panel "input:checked")]
         (get-column-id checkbox))
       set))

(defn get-table-data [table filter-panel]
  {:columns (or (dom/get-local-edn "table-columns")
                (into (get-initial-table-columns table)
                      (get-initial-filter-columns filter-panel)))
   :page-size (some-> (.getAttribute table "data-page-size") parse-long)})

(defn init-giant-table [data filter-panel table locale]
  (let [store (create-foods-store data (get-table-data table filter-panel) (name locale))]
    (init-customizable-table store table filter-panel)
    (init-filter-search store (dom/qs ".mvt-filter-search") locale)
    (swap! store #(assoc % :current (browse-foods % 0)))))
