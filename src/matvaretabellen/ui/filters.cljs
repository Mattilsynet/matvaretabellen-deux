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

(defn get-lists [panel ids]
  (keep #(dom/qs panel (str "ul[data-filter-list-id='" % "']")) ids))

(defn get-checkboxes [panel ids]
  (keep #(dom/qs panel (str "label[data-filter-id='" % "'] input")) ids))

(defn get-rows [table ids]
  (mapcat #(dom/qsa table (str "tr[data-id='" % "']")) ids))

(defn check [checkbox]
  (set! (.-checked checkbox) true))

(defn render-filters [panel prev next]
  (doall (map dom/show (get-lists panel (fd/get-selected next))))
  (doall (map dom/hide (get-lists panel (remove (fd/get-selected next) (fd/get-selected prev))))))

(defn render-table [table prev next]
  (let [next-active (fd/get-active next)]
    (doall (map dom/show (get-rows table next-active)))
    (doall (map dom/hide (get-rows table (remove next-active (fd/get-active prev)))))
    (dom/re-zebra-table table)))

(defn update-ui [panel table prev next]
  (render-filters panel prev next)
  (render-table table prev next))

(defn get-filter-id [el]
  (let [label (.closest el "label")]
    (.getAttribute label "data-filter-id")))

(defn toggle-filter [e store]
  (when (= "checkbox" (some-> e .-target (.getAttribute "type")))
    (let [el (.-target e)]
      (if (.-checked el)
        (swap! store fd/select-id (get-filter-id el))
        (swap! store fd/deselect-id (get-filter-id el))))))

(defn initialize-filter [panel table]
  (let [store (atom (init-filters panel))
        filters @store]
    (add-watch store ::filters (fn [_ _ prev next]
                                 (update-ui panel table prev next)
                                 (persist-filters next)))
    (.addEventListener panel "click" #(toggle-filter % store))
    (when-let [selected (seq (fd/get-selected filters))]
      (doall (map check (get-checkboxes panel selected)))
      (update-ui panel table nil filters))))
