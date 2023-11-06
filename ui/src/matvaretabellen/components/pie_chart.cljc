(ns matvaretabellen.components.pie-chart)

(def r 100)
(def cx r)
(def cy r)
(def d (* r 2))

(defn deg->rad [deg]
  (/ (* Math/PI deg) 180))

(defn render-arc [from-deg to-deg]
  (let [x1 (+ cx (* r (Math/cos (deg->rad from-deg))))
        y1 (+ cy (* r (Math/sin (deg->rad from-deg))))
        x2 (+ cx (* r (Math/cos (deg->rad to-deg))))
        y2 (+ cy (* r (Math/sin (deg->rad to-deg))))
        large-arc-flag (if (> (- to-deg from-deg) 180) 1 0)]
    (str "M " cx " " cy " "                   ;; Move to center
         "L " x1 " " y1 " "                   ;; Line to first point on radius
         "A " r " " r " 0 " large-arc-flag " 1 " x2 " " y2 " " ;; Arc to second point
         "L " cx " " cy                       ;; Line back to center
         )))

(defn assoc-degrees [start-deg slices]
  (let [total (apply + (map :value slices))]
    (loop [[slice & tail] slices
           deg start-deg
           result []]
      (if (nil? slice)
        result
        (let [delta-deg (* 360 (/ (:value slice) total))]
          (recur tail
                 (+ deg delta-deg)
                 (conj result (assoc slice
                                     :from-deg deg
                                     :to-deg (+ deg delta-deg)))))))))

(defn get-slice-id [slice]
  (or (:id slice)
      (str "slice" (hash slice))))

(defn get-hover-attrs [{:keys [from-deg to-deg] :as slice}]
  (let [mid-deg (/ (+ from-deg to-deg) 2)]
    {:class "js-hoverable"
     :data-hover_target_id (get-slice-id slice)
     :data-hover_cx_ratio (/ (+ cx (* r 0.67 (Math/cos (deg->rad mid-deg)))) d)
     :data-hover_cy_ratio (/ (+ cy (* r 0.67 (Math/sin (deg->rad mid-deg)))) d)}))

(defn PieChart [{:keys [slices hoverable?]}]
  [:div {:style {:position "relative"}}
   [:svg.mmm-svg {:viewBox (str "0 0 " d " " d)}
    (for [{:keys [from-deg to-deg color] :as slice} slices]
      [:path (cond-> {:d (render-arc from-deg to-deg)
                      :fill color}
               hoverable? (merge (get-hover-attrs slice)))])]
   (when hoverable?
     (for [slice slices]
       [:div.mtv-hover-popup {:id (get-slice-id slice)}
        (:hover-content slice)]))])
