(ns mmm.components.footer
  (:require [mmm.components.mattilsynet-logo :refer [MattilsynetLogo]]))

(defn Footer [{:keys [cols]}]
  [:footer.mmm-footer
   (for [{:keys [title items text texts header-class]} cols]
     [:div.mmm-footer-col.mmm-vert-layout-m
      [:h3 {:class (or header-class "mmm-h3")} title]
      (when text
        [:p.mmm-p text])
      (for [text texts]
        [:p.mmm-p text])
      (when (seq items)
        [:ul.mmm-ul.mmm-unadorned-list
         (for [{:keys [url text]} items]
           [:li (if url
                  [:a.mmm-link {:href url} text]
                  text)])])])
   [:a {:href "https://www.mattilsynet.no"
        :title "Mattilsynet"}
    (MattilsynetLogo {:class :mmm-logo})]])

(defn CompactSiteFooter []
  (Footer
   {:cols [{:title [:i18n ::about-site]
            :header-class "mmm-h6"
            :texts [[:i18n ::about-text]
                    [:i18n ::api-text]]}
           {:title [:i18n ::about-mattilsynet]
            :header-class "mmm-h6"
            :items [{:url "https://www.mattilsynet.no/varsle"
                     :text [:i18n ::report-to-us]}
                    {:url "https://www.mattilsynet.no/om-mattilsynet/personvernerklaering-og-informasjonskapsler"
                     :text [:i18n ::privacy-and-cookies]}
                    {:text [:i18n ::call-us]
                     :url "tel:+4722400000"}
                    {:url "https://www.mattilsynet.no/kontakt-oss"
                     :text [:i18n ::contact-us]}]}]}))

(defn SiteFooter []
  (Footer
   {:cols [{:title [:i18n ::about-site]
            :items [{:url "https://www.mattilsynet.no/?show=newsletter"
                     :text [:i18n ::newsletter]}
                    {:url "https://www.mattilsynet.no/om-mattilsynet/personvernerklaering-og-informasjonskapsler"
                     :text [:i18n ::privacy-and-cookies]}
                    {:url "https://uustatus.no/nn/erklaringer/publisert/6f6c62f4-caa7-413c-a446-a573a10c243c"
                     :text [:i18n ::accessibility-statement]}
                    {:url "https://www.mattilsynet.no/om-mattilsynet/api"
                     :text [:i18n ::open-data]}]}
           {:title [:i18n ::about-mattilsynet]
            :items [{:url "https://www.mattilsynet.no/om_mattilsynet/"
                     :text [:i18n ::about-us]}
                    {:url "https://www.mattilsynet.no/om-mattilsynet/jobbe-i-mattilsynet"
                     :text [:i18n ::job-openings]}
                    {:url "https://www.mattilsynet.no/kontakt-oss"
                     :text [:i18n ::contact-us]}
                    {:url "https://www.mattilsynet.no/varsle"
                     :text [:i18n ::report-to-us]}]}]}))
