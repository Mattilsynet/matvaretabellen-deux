(ns mmm.components.checkbox)

(defn Checkbox [{:keys [checked? label]}]
  [:label.mmm-checkbox
   [:input {:type "checkbox" :checked (when checked? "true")}]
   [:svg.mmm-svg.checkbox-marker
    {:xmlns "http://www.w3.org/2000/svg"
     :viewBox "0 0 24 24"}
    [:rect {:x "0.5"
            :y "0.5"
            :width "23"
            :height "23"
            :rx "3.5"}]
    [:svg {:x 5 :y 5}
     [:path {:d "M1.82609 4.97933L0 7.36562L6.05115 12.5999L14 3.3002L12.078 1.3999L6.06382 8.86295L1.82609 4.97933Z"
             :fill "white"
             :stroke "none"}]]]
   label])
