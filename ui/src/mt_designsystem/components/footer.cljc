(ns mt-designsystem.components.footer
  (:require [mt-designsystem.components.mattilsynet-logo :refer [MattilsynetLogo]]))

(defn Footer []
  [:footer.footer.footer--regular {:aria-labelledby "footer-title"}
   [:h2#footer-title.inclusively-hidden [:i18n :footer-title]]
   [:div.col
    [:h3 [:i18n ::about-site]]
    [:p [:a {:href "https://www.mattilsynet.no/?show=newsletter"} [:i18n ::newsletter]]]
    [:p [:a {:href "https://www.mattilsynet.no/om-mattilsynet/personvernerklaering-og-informasjonskapsler"} [:i18n ::privacy-and-cookies]]]
    [:p [:a {:href "https://uustatus.no/nn/erklaringer/publisert/6f6c62f4-caa7-413c-a446-a573a10c243c"} [:i18n ::accessibility-statement]]]
    [:p [:a {:href "https://www.mattilsynet.no/om-mattilsynet/api"} [:i18n ::open-data]]]]
   [:div.col
    [:h3 [:i18n ::about-mattilsynet]]
    [:p [:a {:href "https://www.mattilsynet.no/om_mattilsynet/"} [:i18n ::about-us]]]
    [:p [:a {:href "https://www.mattilsynet.no/om-mattilsynet/jobbe-i-mattilsynet"} [:i18n ::job-openings]]]
    [:p [:a {:href "https://www.mattilsynet.no/kontakt-oss"} [:i18n ::contact-us]]]
    [:p [:a {:href "https://www.mattilsynet.no/varsle"} [:i18n ::report-to-us]]]]
   (MattilsynetLogo)])
