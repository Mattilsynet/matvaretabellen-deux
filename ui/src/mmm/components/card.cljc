(ns mmm.components.card)

(defn DetailFocusCard [{:keys [title detail] :as attr}]
  [:a.mmm-card.mmm-link.mmm-vert-layout-s
   (dissoc attr :title :detail)
   [:h2.mmm-nbr.mmm-p title]
   [:p.mmm-h3 detail]])
