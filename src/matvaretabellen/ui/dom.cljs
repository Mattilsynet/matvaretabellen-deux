(ns matvaretabellen.ui.dom
  (:require [clojure.string :as str]))

(defn get-params []
  (when (seq js/location.search)
    (let [raw-tokens (str/split (subs js/location.search 1) #"[=&]")
          tokens (cond-> raw-tokens
                   (odd? (count raw-tokens))
                   butlast)]
      (update-vals (apply hash-map tokens)
                   #(str/replace (js/decodeURIComponent %) #"\+" " ")))))

(defn qsa
  ([selector]
   (seq (js/document.querySelectorAll selector)))
  ([el selector]
   (when el
     (seq (.querySelectorAll el selector)))))

(defn qs
  ([selector]
   (js/document.querySelector selector))
  ([el selector]
   (when el
     (.querySelector el selector))))

(defn visible? [el]
  (and (boolean (some-> el .-offsetParent))
       (let [bcr (.getBoundingClientRect el)]
         (< 0 (* (.-width bcr) (.-height bcr))))))

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
  (when el
    (.remove (.-classList el) class)))

(defn add-class [el class]
  (when el
    (.add (.-classList el) class)))

(defn has-class [el class]
  (when el
    (.contains (.-classList el) class)))

(defn has-attr? [el attr]
  (when el
    (.hasAttribute el attr)))

(defn get-attr [el attr]
  (when el
    (.getAttribute el attr)))

(defn set-attr [el attr v]
  (when el
    (.setAttribute el attr v)))

(defn hidden? [el]
  (when el
    (.hasAttribute el "hidden")))

(defn show [el]
  (when el
    (.removeAttribute el "hidden")))

(defn hide [el]
  (set-attr el "hidden" true))
