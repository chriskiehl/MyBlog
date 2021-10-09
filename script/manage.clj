(ns manage
  (:require [myblog.manage :as m]))



(let [[_ cmd path] *command-line-args*]
  (case cmd
    "add-page"     (do (m/add-page path)
                       (println "All done!"))
    "update-roots" (do (m/update-roots)
                       (println "All content roots updated!"))
    (throw (ex-info "unrecognized argument: " (second *command-line-args*)))))
