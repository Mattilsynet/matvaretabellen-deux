(ns matvaretabellen.ui.food)

(defn from-js
  "Keyborizes most keys except for contituent ids, which are strings with
  keyword-unfriendly characters"
  [food]
  (-> (js->clj food)
      (update-keys keyword)
      (update :constituents
              (fn [constituents]
                (->> (for [[nutrient-id x] constituents]
                       [nutrient-id (update-keys x keyword)])
                     (into {}))))))
