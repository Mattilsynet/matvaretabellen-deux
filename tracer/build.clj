(ns build
  (:require [clojure.tools.build.api :as b]))

(def class-dir "target/classes")
(def uber-file "target/tracer.jar")

(def basis (b/create-basis {:project "deps.edn"}))

(defn clean [& _]
  (b/delete {:path "target"}))

(defn uber [& _]
  (clean)
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :ns-compile '[matvaretabellen.tracer]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'matvaretabellen.tracer}))

(comment
  (clean)
  (uber)
  )
