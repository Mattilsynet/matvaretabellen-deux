(ns matvaretabellen.ui.portions
  (:require [clojure.string :as str]))

(defn count-decimals [n]
  (if (= (Math/floor n) n)
    0
    (count (second (str/split (str n) #"\.")))))

(defn calc-new-portion-fraction [lang portion-size per-100g & [{:keys [decimals]}]]
  (let [orig-decimals (count-decimals per-100g)
        ;; Avoid 0.234 being rounded to 0 when using 0 decimals (kcal, kJ)
        scaled (* portion-size (/ per-100g 100.0))
        decimals (if (= scaled (int scaled))
                   ;; No decimals for whole numbers
                   0
                   ;; Max 2 extra decimals if no decimals where specified
                   (or decimals (+ 2 orig-decimals)))]
    [(.toFixed scaled decimals)
     (.toLocaleString scaled lang #js {:maximumFractionDigits decimals})]))

(defn handle-portion-select-event [e lang portion-elements portion-label-elements]
  (let [value (.-value (.-target e))
        label (some->> (.-options (.-target e))
                       into-array
                       (filter #(= value (.-value %)))
                       first
                       .-innerText
                       str/trim)
        portion-size (js/Number. (.-value (.-target e)))]
    (doseq [elem (seq portion-label-elements)]
      (set! (.-innerHTML elem) label))
    (doseq [elem (seq portion-elements)]
      (let [[scaled formatted] (calc-new-portion-fraction
                                lang
                                portion-size
                                (js/Number. (.getAttribute elem "data-portion"))
                                (when-let [decimals (.getAttribute elem "data-decimals")]
                                  {:decimals (js/Number. decimals)}))]
        (.setAttribute elem "data-value" scaled)
        (set! (.-innerHTML elem) formatted)))))

(defn initialize-portion-selector [lang select-element portion-elements portion-label-elements event-bus]
  (when select-element
    (.addEventListener
     select-element
     "change"
     (fn [e]
       (handle-portion-select-event e lang portion-elements portion-label-elements)
       (reset! event-bus ::changed-portions)))))
