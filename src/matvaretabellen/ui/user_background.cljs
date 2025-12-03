(ns matvaretabellen.ui.user-background
  (:require [matvaretabellen.ui.dom :as dom]
            [matvaretabellen.ui.tracking :as tracking]))

(def local-storage-key "background")

(defn eligible? [local-data]
  (and (nil? local-data) (< 0.5 (rand))))

(defn render-prompt []
  (let [el (js/document.createElement "div")]
    (.addEventListener el "click"
                       (fn [e]
                         (when-let [answer (some-> e .-target (.getAttribute "data-background"))]
                           (tracking/track (str "/stats/bakgrunn/" answer)
                                           (case answer
                                             "fagperson" "Ernæringsfaglig bakgrunn"
                                             "generell-person" "Jobber ikke med ernæring"))
                           (dom/set-local-edn local-storage-key {:answer answer})
                           (.removeChild (.-parentNode el) el))))
    (set! (.-className el) "mmm-container mmm-section")
    (set! (.-innerHTML el)
          (str "<div class=\"mmm-flex-center mmm-flex-gap\">"
               "<p>Jobber du med ernæring?</p>"
               "<div><button data-background=\"fagperson\" class=\"mmm-button mmm-button-small\">Ja</button></div>"
               "<div><button data-background=\"generell-person\" class=\"mmm-button mmm-button-small\">Nei</button></div>"
               "</div>"))
    el))

(defn probe []
  (when (eligible? (dom/get-local-edn local-storage-key))
    (when-let [header (dom/qs "#header")]
      (.insertBefore (.-parentNode header) (render-prompt) (.-nextElementSibling header)))))

(comment

  (probe)

  )
