(ns mmm.components.footer
  (:require [mmm.components.mattilsynet-logo :refer [MattilsynetLogo]]
            [mattilsynet.design :as mtds]))

(defn Footer [{:keys [cols]}]
  [:footer.footer {:data-color "inverted"}
   [:div {:class (mtds/classes :grid) :data-items "300" :data-align "start" :data-size "sm" :data-center "xl"}
    [:a {:href "https://www.mattilsynet.no"
         :aria-label "Mattilsynet"
         :class (mtds/classes :logo)}]
   (for [{:keys [title items text texts]} cols]
     [:div {:class (mtds/classes :grid)}
      [:h2 {:class (mtds/classes :heading) :data-size "sm"} title]
      (when (seq items)
        [:ul {:class (mtds/classes :grid)}
         (for [{:keys [url text]} items]
           [:li (if url
                  [:a {:href url} text]
                  text)])])])]])

(defn CompactSiteFooter [config]
  (Footer
   {:cols [{:title [:i18n ::shortcuts-title]
            :items [{:url "/api/"
                     :text [:i18n ::api-text]}
                    {:url "https://www.mattilsynet.no/om-mattilsynet/personvernerklaering"
                     :text [:i18n ::privacy-and-cookies]}
                    {:url "https://www.mattilsynet.no/"
                     :text "mattilsynet.no"}]}
           {:title [:i18n ::about-mattilsynet]
            :items [{:url "https://www.mattilsynet.no/varsle"
                     :text [:i18n ::report-to-us]}
                    {:url "mailto:matvaretabellen@mattilsynet.no"
                     :text "matvaretabellen@mattilsynet.no"}]}]}))
