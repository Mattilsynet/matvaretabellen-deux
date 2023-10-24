(ns mt-designsystem.elements)

(defn h1 [& args]
  (into [:h1.mvt-h1] args))

(defn h2 [& args]
  (into [:h2.mvt-h2] args))

(defn h3 [& args]
  (into [:h3.mvt-h3] args))

(defn h4 [& args]
  (into [:h4.mvt-h4] args))

(defn h5 [& args]
  (into [:h5.mvt-h5] args))

(defn h6 [& args]
  (into [:h6.mvt-h6] args))

(defn p [& args]
  (into [:p.mvt-p] args))

(defn ul [& args]
  (into [:ul.mvt-ul] args))

(defn ol [& args]
  (into [:ol.mvt-ol] args))

(defn text [& args]
  (into [:div.mvt-text] args))
