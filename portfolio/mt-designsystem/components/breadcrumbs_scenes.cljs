(ns mt-designsystem.components.breadcrumbs-scenes
  (:require [mt-designsystem.components.breadcrumbs :refer [Breadcrumbs]]
            [portfolio.dumdom :as portfolio :refer [defscene]]))

(defscene basic-breadcrumbs
  (Breadcrumbs {:links [{:text "Mattilsynet.no" :url "/"}
                        {:text "Mat" :url "/"}
                        {:text "Matvaretabellen" :url "/"}
                        {:text "Søk i Matvaretabellen"}]}))
