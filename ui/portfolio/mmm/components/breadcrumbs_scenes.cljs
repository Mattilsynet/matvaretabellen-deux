(ns mmm.components.breadcrumbs-scenes
  (:require [mmm.components.breadcrumbs :refer [Breadcrumbs]]
            [mmm.elements :as e]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(portfolio/configure-scenes
 {:title "Brødsmuler"})

(defscene basic-breadcrumbs
  (e/block
   (Breadcrumbs
    {:links [{:text "Mattilsynet.no" :url "/"}
             {:text "Mat" :url "/"}
             {:text "Matvaretabellen" :url "/"}
             {:text "Søk i Matvaretabellen"}]})))
