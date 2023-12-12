(ns matvaretabellen.ui.table
  (:require [matvaretabellen.ui.dom :as dom]
            [matvaretabellen.ui.filter-data :as fd]
            [matvaretabellen.ui.filters :as filters]
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

(defn init-column-settings [store filter-panel]
  (let [columns (::columns @store)]
    (doseq [input (dom/qsa filter-panel "input")]
      (if (columns (get-column-id input))
        (set! (.-checked input) true)
        (set! (.-checked input) false))))
  (->> (fn [e]
         (when-let [id (get-column-id (.-target e))]
           (js/setTimeout
            #(let [update-f (if (.-checked (.-target e)) conj disj)]
               (swap! store update ::columns update-f id))
            ;; Give the checkbox transition time to complete before initiating
            ;; a somewhat heavy render. Without this delay, clicking the checkbox
            ;; will appear laggy
            250)))
       (.addEventListener filter-panel "input")))

(defn browse-foods [{::keys [foods page-size current food-groups]} offset]
  (let [current-foods (cond->> foods
                        food-groups (filter (comp food-groups :foodGroupId)))
        max-n (count current-foods)
        offset (Math/min (- max-n page-size) offset)]
    {:foods current-foods
     :offset offset
     :food-groups (set (map :foodGroupId foods))
     :sort-by (or (:sort-by current) [:foodName :sort.order/asc])
     :n page-size
     :prev (when (< 0 offset)
             [::browse-foods (Math/max 0 (- offset page-size))])
     :next (when (< (+ offset page-size) max-n)
             [::browse-foods (+ offset page-size)])
     :action [::browse-foods offset]}))

(defn filter-by-query [data q]
  (-> (if (<= 3 (.-length q))
        (let [results (search/search-foods q)
              cutoff (* 0.1 (:score (first results)))
              foods (for [x (->> results
                                 ;; Try to loose the most irrelevant ngram noise at
                                 ;; the tail end
                                 (remove #(< (:score %) cutoff)))]
                      (get (::idx data) (:id x)))]
          {:foods (cond->> foods
                    (::food-groups data)
                    (filter (comp (::food-groups data) :foodGroupId)))
           :food-groups (set (map :foodGroupId foods))})
        (browse-foods data 0))
      (assoc :action [::search-foods q])))

(defn dispatch-action [state action]
  (let [[action & args] action]
    (case action
      ::browse-foods (assoc state ::current (apply browse-foods state args))
      ::search-foods (assoc state ::current (apply filter-by-query state args)))))

(defn possibly-update-url [q]
  (when (get (dom/get-params) "q")
    (->> (when-let [query (some-> (not-empty q) js/encodeURIComponent)]
           (str "?q=" query))
         (str js/location.origin js/location.pathname)
         (js/history.replaceState nil ""))))

(defn init-filter-search [store input]
  (when input
    (let [f (debounce #(swap! store (fn [state]
                                      (let [q (.-value (.-target %))]
                                        (possibly-update-url q)
                                        (-> state
                                            (assoc ::current (filter-by-query state q))
                                            fd/clear)))) 250)]
      (.addEventListener input "input" f))))

(defn render-food [el food columns lang]
  (.setAttribute el "data-id" (:foodGroupId food))
  (doseq [td (.-childNodes el)]
    (let [id (.getAttribute td "data-id")]
      (if (columns id)
        (dom/show td)
        (dom/hide td))
      (case id
        "foodName"
        (let [a (.-firstChild td)]
          (set! (.-href a) (:url food))
          (set! (.-innerText a) (or (:shortName food) (:foodName food))))

        "energy"
        (set! (.-innerHTML td)
              (->> [(when-let [kj (:energyKj food)]
                      (str (.toLocaleString kj lang #js {:maximumFractionDigits 0}) "&nbsp;kJ"))
                    (when-let [kcal (:energyKcal food)]
                      (str kcal "&nbsp;kcal"))]
                   (remove empty?)
                   (str/join " / ")))

        (let [el (.-firstChild td)
              decimals (some-> (.getAttribute el "data-decimals") parse-long)
              n (get-in food [:constituents id :quantity 0])]
          (set! (.-innerText el)
                (if n
                  (.toLocaleString n lang #js {:maximumFractionDigits (or decimals 1)})
                  "–"))
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
    "energy" :energyKj
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

(defn render-table [table {:keys [columns current] :as data}]
  (let [[sort-id sort-order] (:sort-by current)
        container (.-parentNode table)]
    (render-rows (dom/qs table "tbody") (.-rowTemplate table) data)
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

(defn get-table-render-data [{::keys [current columns lang]}]
  {:current current
   :columns columns
   :lang lang})

(defn init-button [button store k]
  (when button
    (->> (fn [e]
           (.preventDefault e)
           (when-let [action (k (::current @store))]
             (swap! store dispatch-action action)))
         (.addEventListener button "click"))))

(defn change-sort [store e]
  (when-let [th (.closest (.-target e) "th")]
    (swap!
     store
     (fn [data]
       (let [id (.getAttribute th "data-id")
             [curr-id curr-dir] (-> data ::current :sort-by)
             dir (if (and (= curr-id id)
                          (= curr-dir :sort.order/desc))
                   :sort.order/asc
                   :sort.order/desc)]
         (assoc-in data [::current :sort-by] [id dir]))))))

(defn init-customizable-table [store table]
  (let [rows (dom/qsa table "tbody tr")
        template (first rows)
        tbody (.-parentNode template)]
    (set! (.-rowTemplate table) template)
    (doall (map #(.removeChild tbody %) rows))
    (init-button (dom/qs (.-parentNode table) ".mvt-prev") store :prev)
    (init-button (dom/qs (.-parentNode table) ".mvt-next") store :next)
    (->> #(change-sort store %)
         (.addEventListener (dom/qs table "thead") "click"))))

(defn init-foods-state [data {:keys [columns page-size]} lang]
  (let [foods (->> (seq (.map (js/Object.values data) food/from-js))
                   (sort-by :foodName))]
    {::foods foods
     ::columns (set columns)
     ::page-size (or page-size 250)
     ::idx (into {} (map (juxt :id identity) foods))
     ::lang lang}))

(defn get-initial-table-columns [table]
  (->> (dom/qsa table "thead th")
       (remove #(dom/has-class % "mmm-hidden"))
       (map #(.getAttribute % "data-id"))
       set))

(defn get-initial-filter-columns [filter-panel]
  (->> (for [checkbox (dom/qsa filter-panel "input:checked")]
         (get-column-id checkbox))
       set))

(defn get-local-columns []
  (when-let [columns (dom/get-local-edn "table-columns")]
    (when (and (coll? columns) (not (map? columns)))
      (seq (remove nil? columns)))))

(defn get-table-data [table filter-panel]
  {:columns (or (get-local-columns)
                (into (get-initial-table-columns table)
                      (get-initial-filter-columns filter-panel)))
   :page-size (some-> (.getAttribute table "data-page-size") parse-long)})

(defn select-foods-in-groups [state food-groups]
  (-> (assoc state ::food-groups food-groups)
      (dispatch-action (:action (::current state)))))

(defn toggle-food-groups [filter-panel selected food-groups]
  (let [show? (if food-groups food-groups (constantly true))]
    (doseq [ul (filters/get-lists filter-panel)]
      (let [id (filters/get-list-id ul)]
        (if (show? id)
          (when (selected id)
            (dom/show ul))
          (dom/hide ul))))
    (doseq [checkbox (filters/get-checkboxes filter-panel)]
      (let [id (filters/get-filter-id checkbox)]
        (if (show? id)
          (dom/show (.closest checkbox "li"))
          (do
            (set! (.-checked checkbox) false)
            (dom/hide (.closest checkbox "li"))))))))

(defn on-update [store {:keys [table filter-panel]} prev next]
  (when (filters/render-filters filter-panel prev next)
    (js/setTimeout (fn [_] (swap! store select-foods-in-groups (fd/get-active next))) 100))

  (let [table-data (get-table-render-data next)]
    (when-not (= (get-table-render-data prev) table-data)
      (render-table table table-data)))

  (when (not= (::columns prev) (::columns next))
    (when-let [columns (not-empty (remove nil? (::columns next)))]
      (dom/set-local-edn "table-columns" columns)))

  (when (not= (-> prev ::current :food-groups)
              (-> next ::current :food-groups))
    (->> next ::current :food-groups (mapcat #(fd/get-path next %)) set
         (toggle-food-groups filter-panel (fd/get-selected next)))))

(defn init-components [data locale {:keys [column-panel table filter-panel] :as els} & [{:keys [params]}]]
  (let [store (atom (merge (init-foods-state data (get-table-data table column-panel) (name locale))
                           (when filter-panel
                             (filters/init-filters filter-panel))))]
    (some->> filter-panel (filters/init-filter-panel store))
    (some->> column-panel (init-column-settings store))
    (init-customizable-table store table)
    (init-filter-search store (dom/qs ".mvt-filter-search input"))
    (add-watch store ::self (fn [_ _ old new] (on-update store els old new)))
    store))

(defn select-initial-dataset [store search-input params]
  (if-let [query (not-empty (get params "q"))]
    (when search-input
      (set! (.-value search-input) query)
      (search/on-ready (fn []
                         (swap! store #(assoc % ::current (filter-by-query % query))))))
    (swap! store #(assoc % ::current (browse-foods % 0)))))

(defn init-giant-table [data locale els & [{:keys [params]}]]
  (let [store (init-components data locale els)]
    (select-initial-dataset store (dom/qs ".mvt-filter-search input") params)))
