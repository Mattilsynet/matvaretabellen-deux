(ns ^:figwheel-hooks matvaretabellen.ui.main)

(defn ^:after-load main []
  (prn "The client is ready to do the server's bidding"))

(defn ^:export boot []
  (main))
