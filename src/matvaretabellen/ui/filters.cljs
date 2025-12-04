(ns matvaretabellen.ui.filters
  (:require [matvaretabellen.ui.dom :as dom]
            [matvaretabellen.ui.filter-data :as fd]))

(defn get-session-storage-k []
  (str "filters" (hash js/window.location.pathname)))

(defn get-filter-paths [panel]
  (some->> (dom/qs panel ".mvt-filter-paths")
           .-innerText
           js/JSON.parse
           js->clj))

(defn init-filters [panel]
  (let [paths (get-filter-paths panel)
        selected (dom/get-session-edn (get-session-storage-k))]
    (fd/create paths selected)))

(defn persist-filters
  "Sync filters to session storage for sticky filters across reloads and page
  navigations"
  [{:keys [selected]}]
  (dom/set-session-edn (get-session-storage-k) selected))

(defn get-list-id [ul]
  (.getAttribute ul "data-filter-list-id"))

(defn get-lists [panel & [ids]]
  (if ids
    (keep #(dom/qs panel (str "ul[data-filter-list-id='" % "']")) ids)
    (dom/qsa panel (str "ul[data-filter-list-id]"))))

(defn get-checkboxes [panel & [ids]]
  (if ids
    (keep #(dom/qs panel (str "label[data-filter-id='" % "'] input")) ids)
    (dom/qsa panel (str "label[data-filter-id] input"))))

(defn get-rows [table ids]
  (mapcat #(dom/qsa table (str "tr[data-id='" % "']")) ids))

(defn check [checkbox]
  (set! (.-checked checkbox) true))

(defn render-filters [panel prev next]
  (let [prev-selected (fd/get-selected prev)
        next-selected (fd/get-selected next)]
    (when-not (= prev-selected next-selected)
      (doall (map dom/show (get-lists panel next-selected)))
      (doall (map dom/hide (get-lists panel (remove next-selected prev-selected))))
      true)))

(defn render-table [table prev next]
  (let [next-active (fd/get-active next)]
    (doall (map dom/show (get-rows table next-active)))
    (doall (map dom/hide (get-rows table (remove next-active (fd/get-active prev)))))

(defn get-filter-id [el]
  (let [label (.closest el "label")]
    (.getAttribute label "data-filter-id")))

(defn toggle-filter [e store]
  (when (= "checkbox" (some-> e .-target (.getAttribute "type")))
    (let [el (.-target e)]
      (if (.-checked el)
        (swap! store fd/select-id (get-filter-id el))
        (swap! store fd/deselect-id (get-filter-id el))))))

(defn init-filter-panel [store filter-panel]
  (.addEventListener filter-panel "click" #(toggle-filter % store))
  (let [filters @store]
    (when-let [selected (seq (fd/get-selected filters))]
      (doall (map check (get-checkboxes filter-panel selected)))
      (render-filters filter-panel nil filters))))

(defn initialize-filter [panel table]
  (let [store (atom (init-filters panel))
        filters @store]
    (add-watch store ::filters (fn [_ _ prev next]
                                 (render-filters panel prev next)
                                 (render-table table prev next)
                                 (persist-filters next)))
    (init-filter-panel store panel)
    (when (seq (fd/get-selected filters))
      (render-table table nil @store))))
