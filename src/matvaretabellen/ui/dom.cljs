(ns matvaretabellen.ui.dom
  (:require [clojure.string :as str]))

(defn get-params []
  (when (seq js/location.search)
    (update-vals (apply hash-map (str/split (subs js/location.search 1) #"[=&]"))
                 #(str/replace (js/decodeURIComponent %) #"\+" " "))))

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

(defn show [el]
  (when el
    (remove-class el "mmm-hidden")))

(defn hide [el]
  (when el
    (add-class el "mmm-hidden")))

(defn re-zebra-table [table]
  (remove-class table "mmm-table-zebra")
  (doseq [[i tr] (->> (qsa table "tbody tr")
                      (remove #(has-class % "mmm-hidden"))
                      (map vector (range)))]
    (if (= 0 (mod i 2))
      (add-class tr "mmm-zebra-strip")
      (remove-class tr "mmm-zebra-strip"))))
