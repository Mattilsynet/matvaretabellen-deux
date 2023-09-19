(ns matvaretabellen.components.breadcrumbs-scenes
  (:require [matvaretabellen.components.breadcrumbs :refer [Breadcrumbs]]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene basic-breadcrumbs
  (Breadcrumbs {:links [{:text "Mattilsynet.no" :url "/"}
                        {:text "Mat" :url "/"}
                        {:text "Matvaretabellen" :url "/"}
                        {:text "Søk i Matvaretabellen"}]}))
