(ns matvaretabellen.food-name
  (:require [clojure.string :as str]))

(def name-length 20)
(def stop-words #{"med" "with" ","})

(defn stop-word? [w]
  (stop-words (str/trim w)))

;; Hopefully temporary workaround that the ClojureScript compiler refuses to
;; include ES2018 features in regexes (lookbehind/lookahead)
(def tokenize-re #?(:clj #"(?<!\d),(?!\d)"
                    :cljs #","))

(defn tokenize-name [s]
  (->> (interpose "," (str/split s tokenize-re))
       (mapcat (fn [s]
                 (str/split (str/trim s) #" +")))
       (map-indexed (fn [i w]
                      (cond->> w
                        (and (not= 0 i) (not= "," w)) (str " "))))))

(defn collect-segments [tokens]
  (loop [xs tokens
         segments []]
    (if (empty? xs)
      segments
      (let [stops (take-while stop-word? xs)
            segment-tokens (->> (drop (count stops) xs)
                                (take-while (complement stop-word?))
                                (concat stops))]
        (recur (drop (count segment-tokens) xs) (conj segments segment-tokens))))))

(defn abbreviate-segment [segment]
  [(-> (str/join segment)
       (str/replace #"med" "")
       (str/replace #" og " "/")
       (str/replace #" +" " "))])

(defn compile-segments [{:keys [n abbreviate-words?]} segments]
  (loop [xs segments
         res []
         l 0
         abbreviated? false]
    (if (empty? xs)
      (let [s (str/join res)]
        (if (< (count s) n)
          s
          (str (str/join (take (- n 3) s)) "...")))
      (let [len (+ l (reduce + 0 (map count (first xs))))]
        (cond
          (< len n)
          (recur (next xs) (into res (first xs)) len abbreviated?)

          abbreviated?
          (recur nil (cond-> res
                       (and abbreviate-words? (< l (- n 4)))
                       (concat (first xs)))
                 l abbreviated?)

          :else
          (recur (map abbreviate-segment segments) [] 0 true))))))

(defn shorten-name [food-name & [opt]]
  (->> (tokenize-name food-name)
       collect-segments
       (compile-segments (update opt :n #(or % name-length)))))

(defn find-shared-segments [[i & segments]]
  (when-let [duplicates (->> segments
                             (map #(remove stop-word? %))
                             frequencies
                             (filter #(< 1 (second %)))
                             (map first)
                             seq)]
    [i (set duplicates)]))

(defn shorten-names [names & [opt]]
  (let [opt (cond-> opt
              (not (contains? opt :abbreviate-words?))
              (assoc :abbreviate-words? true))
        name-segments (map (comp collect-segments tokenize-name) names)
        duplicates (->> name-segments
                        (apply map vector (range))
                        (drop 1)
                        (map find-shared-segments)
                        (take-while #(seq (second %)))
                        (into {}))]
    (for [segments name-segments]
      (->> segments
           (map vector (range))
           (remove (fn [[i segment]]
                     (when-let [duplicates (get duplicates i)]
                       (duplicates (remove stop-word? segment)))))
           (map second)
           (compile-segments (update opt :n #(or % name-length)))))))
