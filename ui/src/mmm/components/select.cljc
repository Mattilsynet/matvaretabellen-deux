(ns mmm.components.select)

(defn Select [attrs]
  [:div.mmm-select.mmm-input
   (into [:select (dissoc attrs :options)]
         (:options attrs))
   [:svg.mmm-svg
    {:xmlns "http://www.w3.org/2000/svg"
     :viewBox "0 0 20 20"
     :fill "none"}
    [:g [:path {:d "M18.2143 3.99999L10 12.3636L1.78571 4L0 5.81819L10 16L20 5.81817L18.2143 3.99999Z"}]]]])
