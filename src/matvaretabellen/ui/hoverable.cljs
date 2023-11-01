(ns matvaretabellen.ui.hoverable)

(defn get-hover-target [component event]
  (.querySelector
   component
   (str "#" (.getAttribute (.-target event) "data-hover-target-id"))))

(defn show-target [component event]
  (let [target (get-hover-target component event)
        size (.getBoundingClientRect target)
        container (.-parentElement target)
        bounds (.getBoundingClientRect container)
        cx-ratio (.getAttribute (.-target event) "data-hover-cx-ratio")
        cy-ratio (.getAttribute (.-target event) "data-hover-cy-ratio")]
    (set! (.-top (.-style target)) (str (- (* cy-ratio (.-height bounds))
                                           (.-height size)
                                           10) ;; height of arrow
                                        "px"))
    (set! (.-left (.-style target)) (str (- (* cx-ratio (.-width bounds))
                                            (/ (.-width size) 2)) "px"))
    (.add (.-classList target) "mtv-hover-popup-visible")))

(defn hide-target [component event]
  (-> (get-hover-target component event)
      .-classList
      (.remove "mtv-hover-popup-visible")))

(defn toggle-target [component event]
  (if (-> (get-hover-target component event)
          .-classList
          (.contains "mtv-hover-popup-visible"))
    (hide-target component event)
    (show-target component event)))

(defn set-up [component]
  (when component
    (doseq [element (.querySelectorAll component ".js-hoverable")]
      (.addEventListener element "mouseenter" #(show-target component %) false)
      (.addEventListener element "mouseleave" #(hide-target component %) false)
      (.addEventListener element "click" #(toggle-target component %) false))))
