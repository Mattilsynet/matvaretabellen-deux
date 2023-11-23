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

(defn get-filter-store [panel]
  (let [paths (get-filter-paths panel)
        selected (dom/get-session-edn (get-session-storage-k))]
    (atom (fd/create paths selected))))

(defn sync-filters [{:keys [selected]}]
  (dom/set-session-edn (get-session-storage-k) selected))

(defn get-lists [panel ids]
  (keep #(dom/qs panel (str "ul[data-filter-list-id='" % "']")) ids))

(defn get-checkboxes [panel ids]
  (keep #(dom/qs panel (str "label[data-filter-id='" % "'] input")) ids))

(defn get-rows [table ids]
  (mapcat #(dom/qsa table (str "[data-id='" % "']")) ids))

(defn check [checkbox]
  (set! (.-checked checkbox) true))

(defn re-zebra-table [table]
  (dom/remove-class table "mmm-table-zebra")
  (doseq [[i tr] (->> (dom/qsa table "tbody tr")
                      (remove #(dom/has-class % "mmm-hidden"))
                      (map vector (range)))]
    (if (= 0 (mod i 2))
      (dom/add-class tr "mmm-zebra-strip")
      (dom/remove-class tr "mmm-zebra-strip"))))

(defn update-ui [panel table prev next]
  (let [next-active (fd/get-active next)]
    (doall (map dom/show (get-lists panel (:selected next))))
    (doall (map dom/hide (get-lists panel (remove (:selected next) (:selected prev)))))
    (doall (map dom/show (get-rows table next-active)))
    (doall (map dom/hide (get-rows table (remove next-active (fd/get-active prev)))))
    (re-zebra-table table)))

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
  (let [store (get-filter-store panel)
        filters @store]
    (add-watch store ::filters (fn [_ _ prev next]
                                 (update-ui panel table prev next)
                                 (sync-filters next)))
    (.addEventListener panel "click" #(toggle-filter % store))
    (when (seq (:selected filters))
      (doall (map check (get-checkboxes panel (:selected filters))))
      (update-ui panel table (dissoc filters :selected) filters))))
