(ns matvaretabellen.tracer
  (:require [org.httpkit.server :as http-kit])
  (:gen-class))

(defn handler [req]
  {:status 200
   :body (str "Hello from tracer on " (:uri req))
   :headers {"Content-Type" "text/html; charset=utf-8"}})

(defn start-server [port]
  (http-kit/run-server handler {:port port}))

(defn -main [& _]
  (start-server 8081)
  (println "Started tracer on port 8081"))
