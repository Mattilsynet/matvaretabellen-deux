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

(defn get-session-json [k]
  (try
    (some-> (js/sessionStorage.getItem k)
            js/JSON.parse)
    (catch :default e
      (js/console.error "Unable to read from session storage" e)
      nil)))

(defn get-session-edn [k]
  (some-> (get-session-json k) (js->clj :keywordize-keys true)))

(defn set-session-json [k item]
  (try
    (some->> item
             js/JSON.stringify
             (js/sessionStorage.setItem k))
    (catch :default e
      (js/console.error "Unable to write to session storage" e)
      nil)))

(defn set-session-edn [k v]
  (some->> v clj->js (set-session-json k)))

(defn remove-class [el class]
  (.remove (.-classList el) class))

(defn add-class [el class]
  (.add (.-classList el) class))

(defn has-class [el class]
  (.contains (.-classList el) class))

(defn show [el]
  (remove-class el "mmm-hidden"))

(defn hide [el]
  (add-class el "mmm-hidden"))
