(ns manage
  (:require [myblog.manage :as m]))



(let [[_ cmd path] *command-line-args*]
  (case cmd
    "add-page"     (do (m/add-page path)
                       (println "All done!"))
    "unrecognized argument"))
