(ns easy "Gjør det lett å starte systemet"
  {:targets #{:clj :bb}}
  (:require [babashka.fs :as fs]
            [babashka.process :as p]))

(defn tmux-multi [session-name commands]
  (when-not (fs/which "tmux")
    (println "Kan ikke kjøre " session-name " i tmux fordi tmux ikke er installert.")
    (println)
    (println "Tmux kan installeres med Homebrew:")
    (println)
    (println "    brew install tmux")
    (System/exit 1))
  (try
    (p/shell "tmux new-session -d -s" session-name)
    (catch Exception _e
      (println "Klarte ikke lage en tmux-sesjon for " session-name ".")
      (println "Det kan hende du har en tmux-sesjon som kjører allerede.")
      (System/exit 1)))
  (let [[first-command & remaining-commands] commands]
    (prn 'starting first-command)
    ;; Start the first service on the pane that tmux (not so) graciously created for us
    (p/shell "tmux send-keys" "-t" session-name first-command "C-m")
    ;; Start remaining services on tiles we create by splitting the window.
    (doseq [start-command remaining-commands]
      (prn "starting" start-command)
      (p/shell "tmux split-window -h" "-t" session-name)
      (p/shell "tmux send-keys" "-t" (str session-name ":0.") start-command "C-m")))
  ;; Rebalance panes to be of equal size
  (p/shell "tmux select-layout -t" session-name "tiled")
  ;; Then attach to the process (so that C-c works and we can see stuff)
  (p/exec "tmux attach-session -t" session-name))

(defn preview []
  (tmux-multi "matvaretabellen" ["make start-transactor" "clj -X:dev matnyttig.dev/start"]))

(comment
  ((requiring-resolve 'clojure.repl.deps/sync-deps))
  )
