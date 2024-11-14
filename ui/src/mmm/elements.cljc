(ns mmm.elements)

(defn ^:export h1 [& args]
  (into [:h1.mmm-h1] args))

(defn ^:export h2 [& args]
  (into [:h2.mmm-h2] args))

(defn ^:export h3 [& args]
  (into [:h3.mmm-h3] args))

(defn ^:export h4 [& args]
  (into [:h4.mmm-h4] args))

(defn ^:export h5 [& args]
  (into [:h5.mmm-h5] args))

(defn ^:export h6 [& args]
  (into [:h6.mmm-h6] args))

(defn ^:export p [& args]
  (into [:p.mmm-p] args))

(defn ^:export ul [& args]
  (into [:ul.mmm-ul] args))

(defn ^:export ol [& args]
  (into [:ol.mmm-ol] args))

(defn ^:export text [& args]
  (into [:div.mmm-text] args))

(defn ^:export img [attrs]
  [:img.mmm-img attrs])

(defn ^:export block [& args]
  (into [:div.mmm-block
         (when (map? (first args))
           (first args))]
        (cond-> args
          (map? (first args)) rest)))
