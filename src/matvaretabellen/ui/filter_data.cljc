(ns matvaretabellen.ui.filter-data)

(defn create [paths & [selected]]
  {::selected (set selected)
   ::paths paths
   ::id->path (->> (map (juxt last identity) paths)
                   (into {}))})

(defn get-path [{::keys [id->path]} id]
  (get id->path id))

(defn get-children [paths path]
  (->> paths
       (filter #(= path (drop-last %)))
       (map last)))

(defn get-selected [{::keys [selected]}]
  selected)

(defn get-active
  "Given the currently selected filter checkboxes, computes all effectively active
  filters based on the tree structure (e.g. when all children of a selected
  parent are unselected, they're all considered selected)."
  [{::keys [selected paths id->path]}]
  (set
   (if (empty? selected)
     (keys id->path)
     (mapcat
      (fn [id]
        (let [children (get-children paths (id->path id))]
          (if (empty? (filter selected children))
            (cons id children)
            [id])))
      selected))))

(defn select-id [filters id]
  (update filters ::selected conj id))

(defn deselect-id [filters id]
  (let [ids (cons id (get-children (::paths filters) ((::id->path filters) id)))]
    (update filters ::selected #(reduce disj % ids))))
