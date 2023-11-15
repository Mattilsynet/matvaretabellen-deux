(ns mmm.components.text-input)

(def sizes
  {:small :mmm-input-compact})

(defn TextInput [attrs]
  [:input.mmm-input.mmm-focusable
   {:class (sizes (:size attrs))}
   (dissoc attrs :size)])
