(ns mmm.components.card)

(defn DetailFocusCard [{:keys [title detail href]}]
  [:a.mmm-card.mmm-link.mmm-text.mmm-vert-layout-s
   {:href href}
   [:p.mmm-nbr title]
   [:h2 detail]])
