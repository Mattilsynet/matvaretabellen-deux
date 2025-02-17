(ns mmm.components.footer
  (:require [mmm.components.mattilsynet-logo :refer [MattilsynetLogo]]
            [mattilsynet.design :as mtds]))

(defn Footer2023 [{:keys [cols]}]
  [:footer.mmm-footer
   [:div.mmm-footer-col
    [:a {:href "https://www.mattilsynet.no"
         :title "Mattilsynet"
         :class (mtds/classes :logo :mtds-logo)}]]
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
                  text)])])])])

(defn Footer [{:keys [cols theme] :as data}]
  (if (= "mt2023" theme)
    (Footer2023 data)
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
      (MattilsynetLogo {:class :mmm-logo})]]))

(defn CompactSiteFooter [config]
  (Footer
   {:theme (:app/theme config)
    :cols [{:title [:i18n ::about-site]
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
