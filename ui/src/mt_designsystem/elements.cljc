(ns mt-designsystem.elements)

(defn element [tag & args]
  (into [tag] args))

(defn h1 [& args]
  (element :h1.mvt-h1 args))

(defn h2 [& args]
  (element :h2.mvt-h2 args))

(defn h3 [& args]
  (element :h3.mvt-h3 args))

(defn h4 [& args]
  (element :h4.mvt-h4 args))

(defn h5 [& args]
  (element :h5.mvt-h5 args))

(defn h6 [& args]
  (element :h6.mvt-h6 args))
