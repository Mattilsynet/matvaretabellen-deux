(ns mmm.colors.mattilsynet-90s-scenes
  (:require [clojure.string :as str]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Gammel fargepalett"})

(defn render-colors [color-list]
  (->> color-list
       (map (fn [color-name]
              [:div {:style {:padding 20
                             :margin "20px 0"
                             :background (str "var(--" (name color-name) ")")}}
               [:span {:style {:background "#fff"
                               :display "inline-block"
                               :padding "2px 6px"
                               :border-radius 4
                               :color "#000000"
                               :box-shadow "0 1px 3px rgba(0, 0, 0, 0.2)"}}
                (str "$" (str/replace (name color-name) #"-" "."))]]))))

(defscene mt90s-colors
  [:div
   (render-colors
    [:mt-color-red-100
     :mt-color-red-500
     :mt-color-beige-100
     :mt-color-beige-200
     :mt-color-beige-500
     :mt-color-brown-900
     :mt-color-blue-100
     :mt-color-blue-200
     :mt-color-blue-500
     :mt-color-green-100
     :mt-color-green-500
     :mt-color-orange-100
     :mt-color-orange-500
     :mt-color-white])])
