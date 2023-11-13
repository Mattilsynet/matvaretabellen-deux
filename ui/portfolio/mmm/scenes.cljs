(ns mmm.scenes
  (:require [mmm.components.breadcrumbs-scenes]
            [mmm.components.button-scenes]
            [mmm.components.checkbox-scenes]
            [mmm.components.footer-scenes]
            [mmm.components.pill-scenes]
            [mmm.components.search-input-scenes]
            [mmm.components.select-scenes]
            [mmm.components.site-header-scenes]
            [mmm.components.text-input-scenes]
            [mmm.components.toc-scenes]
            [mmm.elements.image-scenes]
            [mmm.elements.mattilsynet-90s-scenes]
            [mmm.elements.table-scenes]
            [mmm.elements.typography-scenes]
            [mmm.layouts.banner-scenes]
            [mmm.layouts.cards-scenes]
            [mmm.layouts.column-scenes]
            [mmm.layouts.container-scenes]
            [mmm.layouts.flex-scenes]
            [mmm.layouts.media-scenes]
            [mmm.layouts.passepartout-scenes]
            [mmm.layouts.section-scenes]
            [mmm.layouts.vert-layout-scenes]))

:mmm.components.breadcrumbs-scenes/keep
:mmm.components.button-scenes/keep
:mmm.components.checkbox-scenes/keep
:mmm.components.footer-scenes/keep
:mmm.components.pill-scenes/keep
:mmm.components.search-input-scenes/keep
:mmm.components.select-scenes/keep
:mmm.components.site-header-scenes/keep
:mmm.components.text-input-scenes/keep
:mmm.components.toc-scenes/keep
:mmm.elements.image-scenes/keep
:mmm.elements.mattilsynet-90s-scenes/keep
:mmm.elements.table-scenes/keep
:mmm.elements.typography-scenes/keep
:mmm.layouts.banner-scenes/keep
:mmm.layouts.cards-scenes/keep
:mmm.layouts.column-scenes/keep
:mmm.layouts.container-scenes/keep
:mmm.layouts.flex-scenes/keep
:mmm.layouts.media-scenes/keep
:mmm.layouts.passepartout-scenes/keep
:mmm.layouts.section-scenes/keep
:mmm.layouts.vert-layout-scenes/keep

;; Commented out to avoid starting two Portfolio instances
;; while this repo is bundled with Matvaretabellen
#_(def app
  (ui/start!
   {:config
    {:css-paths ["/css/mmm.css"]}}))
