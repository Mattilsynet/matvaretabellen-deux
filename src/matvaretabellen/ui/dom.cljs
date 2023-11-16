(ns matvaretabellen.ui.dom)

(defn qsa
  ([selector]
   (seq (js/document.querySelectorAll selector)))
  ([el selector]
   (seq (.querySelectorAll el selector))))

(defn qs
  ([selector]
   (js/document.querySelector selector))
  ([el selector]
   (.querySelector el selector)))

(defn by-id [id]
  (js/document.getElementById id))

(defn visible? [el]
  (let [bcr (.getBoundingClientRect el)]
    (< 0 (* (.-width bcr) (.-height bcr)))))
