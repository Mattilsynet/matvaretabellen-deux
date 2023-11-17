(ns matvaretabellen.ui.sources
  (:require [matvaretabellen.ui.dom :as dom]))

(defn update-source-toggler [el showing?]
  (if-let [checkbox (dom/qs el "input")]
    (set! (.-checked checkbox) showing?)
    (if showing?
      (.remove (.-classList el) "mmm-button-secondary")
      (.add (.-classList el) "mmm-button-secondary"))))

;; There's a script tag at the beginning of the body tag that sets the
;; mvt-source-hide class immediately to avoid any flickering. Please keep it in
;; mind if you change this.
(defn toggle-sources [_e selector]
  (let [show? (boolean (js/document.body.classList.contains "mvt-source-hide"))]
    (doseq [el (dom/qsa selector)]
      (update-source-toggler el show?))
    (js/localStorage.setItem "show-sources" show?)
    (if show?
      (js/document.body.classList.remove "mvt-source-hide")
      (js/document.body.classList.add "mvt-source-hide"))))

(defn initialize-source-toggler [selector]
  (let [showing? (= "true" (js/localStorage.getItem "show-sources"))]
    (when-not showing?
      (js/document.body.classList.add "mvt-source-hide"))
    (doseq [toggler (dom/qsa selector)]
      (update-source-toggler toggler showing?)
      (if-let [checkbox (dom/qs toggler "input")]
        (.addEventListener checkbox "input" #(toggle-sources % selector))
        (.addEventListener toggler "click" #(toggle-sources % selector))))))
