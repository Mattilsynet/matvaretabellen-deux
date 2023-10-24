(ns mt-designsystem.elements)

(defn ^:export h1 [& args]
  (into [:h1.mvt-h1] args))

(defn ^:export h2 [& args]
  (into [:h2.mvt-h2] args))

(defn ^:export h3 [& args]
  (into [:h3.mvt-h3] args))

(defn ^:export h4 [& args]
  (into [:h4.mvt-h4] args))

(defn ^:export h5 [& args]
  (into [:h5.mvt-h5] args))

(defn ^:export h6 [& args]
  (into [:h6.mvt-h6] args))

(defn ^:export p [& args]
  (into [:p.mvt-p] args))

(defn ^:export ul [& args]
  (into [:ul.mvt-ul] args))

(defn ^:export ol [& args]
  (into [:ol.mvt-ol] args))

(defn ^:export text [& args]
  (into [:div.mvt-text] args))
