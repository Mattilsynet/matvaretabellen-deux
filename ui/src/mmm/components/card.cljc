;; TODO EIRIK: Not in use anymore
(ns mmm.components.card)

(defn DetailFocusCard [{:keys [title detail] :as attr}]
  [:a.mmm-card.mmm-link.mmm-vert-layout-s.mmm-cols-d3m2
   (dissoc attr :title :detail)
   [:h2.mmm-nbr.mmm-p title]
   [:p.mmm-h3 detail]])
