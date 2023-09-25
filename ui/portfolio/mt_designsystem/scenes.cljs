(ns mt-designsystem.scenes
  (:require [mt-designsystem.components.breadcrumbs-scenes]
            [mt-designsystem.components.search-input-scenes]
            [mt-designsystem.components.site-header-scenes]))

:mt-designsystem.components.breadcrumbs-scenes/keep
:mt-designsystem.components.search-input-scenes/keep
:mt-designsystem.components.site-header-scenes/keep

;; Commented out to avoid starting two Portfolio instances
;; while this repo is bundled with Matvaretabellen
#_(def app
  (ui/start!
   {:config
    {:css-paths ["/css/mt-designsystem.css"]}}))
