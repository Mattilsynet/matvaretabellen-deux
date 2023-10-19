(ns matvaretabellen.i18n.nb)

(def dictionary
  (->>
   [#:frontpage
    {:search-label "Søk i Matvaretabellen"
     :search-button "Søk"
     }

    #:matvaretabellen.crumbs
    {:all-food-groups "Alle matvaregrupper"
     :food-groups-url "/matvaregrupper/"
     :home "Hjem"
     :search-label "Søk i Matvaretabellen"
     }

    #:food
    {:adi-title "Anbefalt daglig inntak (ADI)"
     :carbohydrates-title "Karbohydrater"
     :category [:fn/str "Kategori: {{:category}}"]
     :description-title "Beskrivelse av matvaren"
     :energy-title "Sammensetning og energiinnhold"
     :fat-title "Fettsyrer"
     :food-id [:fn/str "Matvare-ID: {{:id}}"]
     :latin-name [:fn/str "Latin: {{:food/latin-name}}"]
     :minerals-title "Mineraler"
     :nutrition-title "Næringsinnhold"
     :toc-title "Innhold"
     :vitamins-title "Vitaminer"
     }

    #:food-groups
    {:all-food-groups "Alle matvaregrupper"
     }]
   (apply merge)))
