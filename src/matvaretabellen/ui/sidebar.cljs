(ns matvaretabellen.ui.sidebar
  (:require [matvaretabellen.ui.dom :as dom]))

(defn initialize-sidebar [toggler sidebar]
  (if (dom/visible? toggler)
    (when-not (.contains (.-classList sidebar) "mmm-sidebar")
      (.add (.-classList sidebar) "mmm-sidebar")
      (.add (.-classList sidebar) "mmm-sidebar-closed"))
    (when (.contains (.-classList sidebar) "mmm-sidebar")
      (.remove (.-classList sidebar) "mmm-sidebar")
      (.remove (.-classList sidebar) "mmm-sidebar-closed"))))

(defn toggle-sidebar [sidebar]
  (if (.contains (.-classList sidebar) "mmm-sidebar-closed")
    (.remove (.-classList sidebar) "mmm-sidebar-closed")
    (.add (.-classList sidebar) "mmm-sidebar-closed")))

(defn get-sidebar [toggler]
  (dom/qs (.getAttribute toggler "data-sidebar-target")))

(defn initialize [selector]
  (let [togglers (dom/qsa selector)]
    (doseq [toggler togglers]
      (let [sidebar (get-sidebar toggler)]
        (initialize-sidebar toggler sidebar)
        (.addEventListener toggler "click" #(toggle-sidebar sidebar))))
    (->> (fn [_e]
           (doseq [toggler togglers]
             (initialize-sidebar toggler (get-sidebar toggler))))
         (.addEventListener js/window "resize"))))
