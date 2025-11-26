(ns matvaretabellen.mtx-test
  (:require [clojure.test :refer [deftest is]]
            [matvaretabellen.mtx :as mtx]))

(def term
  '{:tag :term,
    :attrs {},
    :content
    ({:tag :termDesc,
      :attrs {},
      :content
      ({:tag :termCode, :attrs {}, :content ("A0MNA")}
       {:tag :termExtendedName,
        :attrs {},
        :content ("Red Sea houndfish (as animal)")}
       {:tag :termScopeNote,
        :attrs {},
        :content
        ("Live animal of the taxonomic species Tylosurus choram, within the Family Belonidae. The organisms are commonly known as Red Sea houndfish. The part considered is by default the whole living organism.£http://www.fishbase.se/summary/Tylosurus-choram.html£http://www.marinespecies.org/aphia.php?p=taxlist&tName=Tylosurus choram")})}
     {:tag :termVersion,
      :attrs {},
      :content
      ({:tag :version, :attrs {}, :content ("8.9")}
       {:tag :lastUpdate, :attrs {}, :content ("2017-07-20T17:46:33")}
       {:tag :validFrom, :attrs {}, :content ("2015-03-16T23:19:07")}
       {:tag :status, :attrs {}, :content ("APPROVED")})}
     {:tag :hierarchyAssignments,
      :attrs {},
      :content
      ({:tag :hierarchyAssignment,
        :attrs {},
        :content
        ({:tag :hierarchyCode, :attrs {}, :content ("MTX")}
         {:tag :parentCode, :attrs {}, :content ("A08VE")}
         {:tag :order, :attrs {}, :content ("3")}
         {:tag :reportable, :attrs {}, :content ("true")})}
       {:tag :hierarchyAssignment,
        :attrs {},
        :content
        ({:tag :hierarchyCode, :attrs {}, :content ("source")}
         {:tag :parentCode, :attrs {}, :content ("A08VE")}
         {:tag :order, :attrs {}, :content ("3")}
         {:tag :reportable, :attrs {}, :content ("true")})})}
     {:tag :implicitAttributes,
      :attrs {},
      :content
      ({:tag :implicitAttribute,
        :attrs {},
        :content
        ({:tag :attributeCode, :attrs {}, :content ("alpha3Code")}
         {:tag :attributeValues,
          :attrs {},
          :content ({:tag :attributeValue, :attrs {}, :content ("TBH")})})}
       {:tag :implicitAttribute,
        :attrs {},
        :content
        ({:tag :attributeCode, :attrs {}, :content ("A01")}
         {:tag :attributeValues,
          :attrs {},
          :content
          ({:tag :attributeValue, :attrs {}, :content ("Tylosurus choram")})})}
       {:tag :implicitAttribute,
        :attrs {},
        :content
        ({:tag :attributeCode, :attrs {}, :content ("taxonomicCode")}
         {:tag :attributeValues,
          :attrs {},
          :content
          ({:tag :attributeValue, :attrs {}, :content ("T1470101309")})})}
       {:tag :implicitAttribute,
        :attrs {},
        :content
        ({:tag :attributeCode, :attrs {}, :content ("termType")}
         {:tag :attributeValues,
          :attrs {},
          :content ({:tag :attributeValue, :attrs {}, :content ("n")})})}
       {:tag :implicitAttribute,
        :attrs {},
        :content
        ({:tag :attributeCode, :attrs {}, :content ("allFacets")}
         {:tag :attributeValues,
          :attrs {},
          :content
          ({:tag :attributeValue, :attrs {}, :content ("A0MNA#F02.A0EJV")})})}
       {:tag :implicitAttribute,
        :attrs {},
        :content
        ({:tag :attributeCode, :attrs {}, :content ("ISSCAAP")}
         {:tag :attributeValues,
          :attrs {},
          :content ({:tag :attributeValue, :attrs {}, :content ("I.37")})})}
       {:tag :implicitAttribute,
        :attrs {},
        :content
        ({:tag :attributeCode, :attrs {}, :content ("detailLevel")}
         {:tag :attributeValues,
          :attrs {},
          :content ({:tag :attributeValue, :attrs {}, :content ("E")})})})})})

(deftest parse-term
  (is (= (mtx/parse-term term)
         {:foodex2.term/code "A0MNA"
          :foodex2.term/name "Red Sea houndfish (as animal)"
          :foodex2.term/note "Live animal of the taxonomic species Tylosurus choram, within the Family Belonidae. The organisms are commonly known as Red Sea houndfish. The part considered is by default the whole living organism.£http://www.fishbase.se/summary/Tylosurus-choram.html£http://www.marinespecies.org/aphia.php?p=taxlist&tName=Tylosurus choram"})))

(comment
  (set! *print-namespace-maps* false)
  )
