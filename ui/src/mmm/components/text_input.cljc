(ns mmm.components.text-input)

(def sizes
  {:small :mmm-input-compact})

(defn TextInput [attrs]
  (let [size-class (sizes (:size attrs))]
    [:input.mmm-input.mmm-focusable
     (cond-> (dissoc attrs :size)
       size-class
       (update :class #(if (coll? %)
                         (conj % size-class)
                         [% size-class])))]))
