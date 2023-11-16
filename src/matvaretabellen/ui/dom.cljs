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

(defn visible? [el]
  (let [bcr (.getBoundingClientRect el)]
    (< 0 (* (.-width bcr) (.-height bcr)))))

(defn get-local-json [k]
  (try
    (some-> (js/localStorage.getItem k)
            js/JSON.parse)
    (catch :default e
      (js/console.error "Unable to read from local storage" e)
      nil)))

(defn get-local-edn [k]
  (some-> (get-local-json k) js->clj))

(defn set-local-json [k v]
  (try
    (some->> v
             js/JSON.stringify
             (js/localStorage.setItem k))
    (catch :default e
      (js/console.error "Unable to write to local storage" e)
      nil)))

(defn set-local-edn [k v]
  (some->> v clj->js (set-local-json k)))
