(ns matvaretabellen.layout
  (:require [mmm.components.footer :refer [CompactSiteFooter]]))

(defn layout [context head body]
  [:html {:class "mmm"}
   head
   (into
    body
    (list [:div.mmm-container.mmm-section
           (CompactSiteFooter)]
          [:img {:src (str "https://mattilsynet.matomo.cloud/matomo.php?idsite="
                           (:matomo/site-id context) "&rec=1")
                 :style "border:0"
                 :alt ""}]))])
