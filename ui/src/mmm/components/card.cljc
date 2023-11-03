(ns mmm.components.card)

(defn DetailFocusCard [{:keys [title detail href class]}]
  [:a.mmm-card.mmm-link.mmm-vert-layout-s
   {:href href :class class}
   [:h2.mmm-nbr.mmm-p title]
   [:p.mmm-h3 detail]])
