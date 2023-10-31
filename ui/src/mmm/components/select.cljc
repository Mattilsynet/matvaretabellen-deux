(ns mmm.components.select)

(defn Select [attrs]
  (into [:select.mmm-input (dissoc attrs :options)]
        (:options attrs)))
