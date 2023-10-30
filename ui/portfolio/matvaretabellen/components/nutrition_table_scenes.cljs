(ns matvaretabellen.components.nutrition-table-scenes
  (:require [matvaretabellen.components.nutrition-table :refer [NutritionTable]]
            [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(def data
  {:title "Næringsinnhold"
   :subtitle "100 g vare gir ca.:"
   :categories [{:label "Energi:" :content "1503 kJ / 358 kcal"}
                {:label "Fett:" :content "7,5 g"
                 :subcategories [{:label "hvorav"}
                                 {:label "mettede fettsyrer" :content "1,6 g"}
                                 {:label "enumettede fettsyrer" :content "2,7 g"}
                                 {:label "flerumettede fettsyrer" :content "2,7 g"}]}
                {:label "Karbohydrat:" :content "53 g"
                 :subcategories [{:label "hvorav sukkerarter" :content "3,1 g"}]}
                {:label "Fiber:" :content "15,0 g"}
                {:label "Protein:" :content "12 g"}
                {:label "Salt:" :content "0 g"}]})

(defscene basic-table
  (e/block (NutritionTable data)))

(defscene small-table
  (e/block (NutritionTable (assoc data :class :small))))
