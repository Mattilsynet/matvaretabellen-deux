(ns matvaretabellen.tracer
  (:gen-class)
  (:require [clojure.pprint :as pprint]
            [dumdom.string :as dumdom]
            [org.httpkit.server :as http-kit]))

(defonce store (atom {}))

(defn render-infos []
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body
   (dumdom/render
    [:div
     [:h1 "Sporing av feilsituasjoner på Matvaretabellen"]
     [:pre (with-out-str (pprint/pprint @store))]])})

(defn ++ [n]
  (if (nil? n) 1 (inc n)))

(defn handler [req]
  (cond
    (= "/tracer/no-script/" (:uri req))
    (do (swap! store update "No script" ++)
        {:status 200 :body "OK"})

    (= "/tracer/infos/" (:uri req))
    (render-infos)

    :else {:status 200 :body "OK"})) ;; ikke noe stress, alt er greit!

(defn start-server [port]
  (http-kit/run-server handler {:port port}))

(defn -main [& _]
  (start-server 8081)
  (println "Started tracer on port 8081"))
