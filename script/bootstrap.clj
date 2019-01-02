(ns bootstrap
  (:require [myblog.persistence.tables :as tables]
            [environ.core :refer [env]]))


; Sets up the required Dynamo table either locally (dev profile)
; or remotely (prod)


; this ends up being during uberjar compilation (bad), and I
; can't figure out how to NOT make lein-exec do that (also bad), so a cli arg
; is passed as a dumb flag to make sure we only run it when we mean to
(when (= (second *command-line-args*) "actually-run-this")
  (let [creds {:access-key (:aws-access-key env)
               :secret-key (:aws-secret-key env)
               :endpoint (:aws-endpoint env)
               :region (:aws-region env)}]
    (println (tables/create-articles-table creds (:table-name env)))))

