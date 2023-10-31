(ns mmm.components.footer
  (:require [mmm.components.mattilsynet-logo :refer [MattilsynetLogo]]))

(defn Footer [{:keys [cols]}]
  [:footer.mmm-footer
   (for [{:keys [title links]} cols]
     [:div.mmm-text.mmm-footer-col
      [:h3 title]
      [:ul.mmm-unadorned-list
       (for [{:keys [url text]} links]
         [:li [:a {:href url} text]])]])
   (MattilsynetLogo)])

(defn SiteFooter []
  (Footer
   {:cols [{:title [:i18n ::about-site]
            :links [{:url "https://www.mattilsynet.no/?show=newsletter"
                     :text [:i18n ::newsletter]}
                    {:url "https://www.mattilsynet.no/om-mattilsynet/personvernerklaering-og-informasjonskapsler"
                     :text [:i18n ::privacy-and-cookies]}
                    {:url "https://uustatus.no/nn/erklaringer/publisert/6f6c62f4-caa7-413c-a446-a573a10c243c"
                     :text [:i18n ::accessibility-statement]}
                    {:url "https://www.mattilsynet.no/om-mattilsynet/api"
                     :text [:i18n ::open-data]}]}
           {:title [:i18n ::about-mattilsynet]
            :links [{:url "https://www.mattilsynet.no/om_mattilsynet/"
                     :text [:i18n ::about-us]}
                    {:url "https://www.mattilsynet.no/om-mattilsynet/jobbe-i-mattilsynet"
                     :text [:i18n ::job-openings]}
                    {:url "https://www.mattilsynet.no/kontakt-oss"
                     :text [:i18n ::contact-us]}
                    {:url "https://www.mattilsynet.no/varsle"
                     :text [:i18n ::report-to-us]}]}]}))