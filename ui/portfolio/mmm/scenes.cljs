(ns mmm.scenes
  (:require [mmm.colors.mattilsynet-90s-scenes]
            [mmm.components.breadcrumbs-scenes]
            [mmm.components.button-scenes]
            [mmm.components.footer-scenes]
            [mmm.components.search-input-scenes]
            [mmm.components.select-scenes]
            [mmm.components.site-header-scenes]
            [mmm.components.text-input-scenes]
            [mmm.components.toc-scenes]
            [mmm.elements.image-scenes]
            [mmm.elements.table-scenes]
            [mmm.elements.typography-scenes]
            [mmm.layouts.banner-scenes]
            [mmm.layouts.cards-scenes]
            [mmm.layouts.column-scenes]
            [mmm.layouts.container-scenes]
            [mmm.layouts.media-scenes]
            [mmm.layouts.section-scenes]
            [mmm.layouts.threecol-scenes]))

:mmm.colors.mattilsynet-90s-scenes/keep
:mmm.components.breadcrumbs-scenes/keep
:mmm.components.button-scenes/keep
:mmm.components.footer-scenes/keep
:mmm.components.search-input-scenes/keep
:mmm.components.select-scenes/keep
:mmm.components.site-header-scenes/keep
:mmm.components.text-input-scenes/keep
:mmm.components.toc-scenes/keep
:mmm.elements.image-scenes/keep
:mmm.elements.table-scenes/keep
:mmm.elements.typography-scenes/keep
:mmm.layouts.banner-scenes/keep
:mmm.layouts.cards-scenes/keep
:mmm.layouts.column-scenes/keep
:mmm.layouts.container-scenes/keep
:mmm.layouts.media-scenes/keep
:mmm.layouts.section-scenes/keep
:mmm.layouts.threecol-scenes/keep

;; Commented out to avoid starting two Portfolio instances
;; while this repo is bundled with Matvaretabellen
#_(def app
  (ui/start!
   {:config
    {:css-paths ["/css/mmm.css"]}}))
