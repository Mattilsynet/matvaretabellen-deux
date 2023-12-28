(ns matvaretabellen.tracer
  (:gen-class)
  (:require [clojure.pprint :as pprint]
            [dumdom.string :as dumdom]
            [lambdaisland.uri :refer [uri query-map]]
            [org.httpkit.server :as http-kit])
  (:import (java.time ZonedDateTime ZoneId)))

(defonce store (atom {}))

(defn render-infos []
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body
   (dumdom/render
    [:div
     [:h1 "Sporing av feilsituasjoner på Matvaretabellen"]
     [:p "Her er det strikk og binders, og vi er helt tomme for strikk. Slenger
     ut noen følere for å se etter problemer med klienten i jula. God jul, da!"]
     [:pre (with-out-str (pprint/pprint @store))]])})

(defn trace [{:keys [num]}]
  {:num (if num (inc num) 1)
   :last-date (str (.toLocalDateTime (ZonedDateTime/now (ZoneId/of "Europe/Oslo"))))})

(defn handler [req]
  (let [uri (uri (:uri req)) ;; Bjarne, Bjarne, Bjarne
        ua (get-in req [:headers "user-agent"])]
    (case (:path uri)
      "/tracer/no-script/"
      (do (swap! store update-in ["No script" ua] trace)
          {:status 200 :body "OK"})

      "/tracer/report/"
      (let [error (or (:error (query-map (str "/?" (:query-string req))))
                      ["Overraskende ingen error på rapporten"
                       (keys req)
                       (:uri req)
                       (:query-string req)])]
        (swap! store update-in [error ua] trace)
        {:status 200 :body "OK"})

      "/tracer/infos/"
      (render-infos)

      ;; bomma litt?
      (do (swap! store assoc "Traff ikke i case'n, gett" (:path uri))
          {:status 200 :body "OK"}))))

(defn start-server [port]
  (http-kit/run-server handler {:port port}))

(defn -main [& _]
  (start-server 8081)
  (println "Started tracer on port 8081"))
