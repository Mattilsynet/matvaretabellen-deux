(ns matvaretabellen.ui.table
  (:require [clojure.string :as str]
            [matvaretabellen.ui.dom :as dom]
            [matvaretabellen.ui.filter-data :as fd]
            [matvaretabellen.ui.filters :as filters]
            [matvaretabellen.ui.food :as food]
            [matvaretabellen.ui.search :as search]
            [matvaretabellen.ui.toggler :as toggler]
            [matvaretabellen.ui.tracking :as tracking]))

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
    (let [url (->> (when-let [query (some-> (not-empty q) js/encodeURIComponent)]
                     (str "?q=" query))
                   (str js/location.pathname))]
      (js/history.replaceState nil "" (str js/location.origin url))
      (tracking/track url js/document.title))))

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
        (let [a (dom/qs td "a")]
          (set! (.-href a) (:url food))
          (set! (.-innerText a) (or (:shortName food) (:foodName food))))

        "energy"
        (set! (.-innerHTML td)
              (->> [(when-let [kj (:energyKj food)]
                      (str (.toLocaleString kj lang #js {:maximumFractionDigits 0}) " kJ"))
                    (when-let [kcal (:energyKcal food)]
                      (str kcal " kcal"))]
                   (remove empty?)
                   (str/join " / ")))

        "download"
        (.setAttribute (.-firstChild td) "data-food-id" (:id food))

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
        (when-let [icon (dom/qs th ".mvt-sort-icon")]
          (let [selector (if (= sort-id id)
                           (if (= sort-order :sort.order/asc)
                             ".mvt-asc"
                             ".mvt-desc")
                           ".mvt-sort")]
            (set! (.-innerHTML icon) "")
            (.appendChild icon (.cloneNode (dom/qs container selector) true))))))
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
  (let [th (.closest (.-target e) "th")]
    (when (dom/qs th ".mvt-sort-icon")
      (swap!
       store
       (fn [data]
         (let [id (.getAttribute th "data-id")
               [curr-id curr-dir] (-> data ::current :sort-by)
               dir (if (and (= curr-id id)
                            (= curr-dir :sort.order/desc))
                     :sort.order/asc
                     :sort.order/desc)]
           (assoc-in data [::current :sort-by] [id dir])))))))

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

(defn load-selected-foods []
  (set (dom/get-local-edn "selected-foods")))

(defn save-selected-foods [selected]
  (dom/set-local-edn "selected-foods" selected))

(defn init-foods-state [data {:keys [columns page-size]} lang]
  (let [foods (->> (seq (.map (js/Object.values data) food/from-js))
                   (sort-by :foodName))]
    {::foods foods
     ::columns (set columns)
     ::page-size (or page-size 250)
     ::selected (load-selected-foods)
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

(def columns-k
  (str js/location.pathname "table-columns"))

(defn persist-columns [columns]
  (dom/set-local-edn columns-k columns))

(defn get-local-columns []
  (when-let [columns (dom/get-local-edn columns-k)]
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

(defn disable-button [el]
  (dom/add-class el "mmm-button-disabled"))

(defn enable-button [el]
  (dom/remove-class el "mmm-button-disabled"))

(defn render-download-button [foods button]
  (when-let [el (dom/qs button ".mvt-num-foods")]
    (set! (.-innerText el) (count foods)))
  (if (< 0 (count foods))
    (enable-button button)
    (disable-button button)))

(defn render-clear-download-button [selected button]
  (if (< 0 (count selected))
    (dom/show button)
    (dom/hide button)))

(defn render-downloads [{:keys [download-buttons clear-download-buttons table]} selected]
  (doseq [button download-buttons]
    (render-download-button selected button))
  (doseq [button clear-download-buttons]
    (render-clear-download-button selected button))
  (doseq [button (dom/qsa table "tbody .mvt-add-to-list")]
    (->> (get selected (.getAttribute button "data-food-id"))
         (toggler/toggle-icon-button button))))

(defn on-update [store {:keys [table filter-panel] :as els} prev next]
  (when (filters/render-filters filter-panel prev next)
    (js/setTimeout (fn [_] (swap! store select-foods-in-groups (fd/get-active next))) 100))

  (let [table-data (get-table-render-data next)]
    (when-not (= (get-table-render-data prev) table-data)
      (render-table table table-data)
      (render-downloads els (::selected next))))

  (when (not= (::columns prev) (::columns next))
    (when-let [columns (not-empty (remove nil? (::columns next)))]
      (persist-columns columns)))

  (when (not= (-> prev ::current :food-groups)
              (-> next ::current :food-groups))
    (->> next ::current :food-groups (mapcat #(fd/get-path next %)) set
         (toggle-food-groups filter-panel (fd/get-selected next))))

  (when (not= (::selected prev) (::selected next))
    (render-downloads els (::selected next))))

(defn export-csv [{::keys [foods selected columns]} column-order locale]
  (->> (let [fields (filter (comp (disj columns "download") first) column-order)
             ids (map (comp keyword first) fields)
             lang (name locale)]
         (->> (cons (->> (for [[id header] fields]
                           (if (= id "energy")
                             (str header " (kJ);" header " (kcal)")
                             header))
                         (str/join ";"))
                    (for [food (filter (comp (set selected) :id) foods)]
                      (->> (for [id ids]
                             (case id
                               :foodName
                               (:foodName food)

                               :energy
                               (str (:energyKj food) ";" (:energyKcal food))

                               (get-in food [:constituents (name id) :quantity 0])))
                           (map #(cond-> %
                                   (number? %)
                                   (.toLocaleString lang #js {:maximumFractionDigits 2})))
                           (str/join ";"))))
              (str/join "\n")))
       js/encodeURIComponent
       (str "data:text/csv;charset=UTF-8,\uFEFF")))

(defn get-column-order [table]
  (mapv (fn [el]
          [(.getAttribute el "data-id")
           (.-innerText el)])
        (dom/qsa table "thead th")))

(defn init-download-button [store table button locale]
  (let [column-order (get-column-order table)]
    (->> (fn [e]
           (if (dom/has-class button "mmm-button-disabled")
             (.preventDefault e)
             (set! (.-href button) (export-csv @store column-order locale))))
         (.addEventListener button "click")))
  (render-download-button (::selected @store) button)
  (dom/show button))

(defn toggle-every-icon-button [ids active]
  (doseq [id ids]
    (doseq [el (dom/qsa (str ".mmm-icon-button[data-food-id='" id "']"))]
      (toggler/toggle-icon-button el active))))

(defn init-stage-download-buttons [store table]
  (->> (fn [e]
         (when-let [icon-button (some-> (.-target e) (.closest ".mvt-add-to-list"))]
           (.preventDefault e)
           (.stopPropagation e)
           (let [{::keys [selected current]} @store
                 id (.getAttribute icon-button "data-food-id")
                 ids (if id #{id} (set (map :id (:foods current))))
                 selected? (every? selected ids)]
             (swap! store update ::selected (if selected? #(set (remove ids %)) #(into % ids)))
             (if id
               (toggler/toggle-icon-button icon-button (not selected?))
               (toggle-every-icon-button ids (not selected?))))))
       (.addEventListener table "click")))

(defn init-clear-download-button [store button]
  (->> (fn [e]
         (when (some-> (.-target e) (.closest ".mvt-clear-downloads"))
           (toggle-every-icon-button (::selected @store) false)
           (swap! store assoc ::selected #{})))
       (.addEventListener button "click")))

(defn init-download-buttons [store {:keys [table download-buttons clear-download-buttons]} locale]
  (doseq [button download-buttons]
    (init-download-button store table button locale))
  (doseq [button clear-download-buttons]
    (init-clear-download-button store button))
  (init-stage-download-buttons store table))

(defn init-components [data locale {:keys [column-panel table filter-panel] :as els}]
  (let [store (atom (merge (init-foods-state data (get-table-data table column-panel) (name locale))
                           (when filter-panel
                             (filters/init-filters filter-panel))))]
    (some->> filter-panel (filters/init-filter-panel store))
    (some->> column-panel (init-column-settings store))
    (init-customizable-table store table)
    (init-filter-search store (dom/qs ".mvt-filter-search input"))
    (add-watch store ::self (fn [_ _ old new] (on-update store els old new)))
    (init-download-buttons store els locale)
    store))

(defn select-initial-dataset [store search-input params]
  (if-let [query (not-empty (get params "q"))]
    (when search-input
      (set! (.-value search-input) query)
      (search/on-ready (fn []
                         (swap! store #(assoc % ::current (filter-by-query % query))))))
    (swap! store #(assoc % ::current (browse-foods % 0)))))

(defn init-giant-table [data locale els & [{:keys [params]}]]
  (doseq [el (dom/qsa (:table els) "th[data-id='download']")]
    (dom/show el))
  (let [store (init-components data locale els)]
    (select-initial-dataset store (dom/qs ".mvt-filter-search input") params)
    (render-downloads els (::selected @store))
    (add-watch store ::save-selected (fn [_ _ old next]
                                       (when-not (= (::selected old) (::selected next))
                                         (save-selected-foods (::selected next))))))
  (when-let [form (.closest (dom/qs ".mvt-filter-search input") "form")]
    (.addEventListener form "submit" (fn [e] (.preventDefault e)))))
