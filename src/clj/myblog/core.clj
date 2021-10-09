(ns myblog.core
  (:gen-class)
  (:require [hiccup.core :refer :all]
            [environ.core :refer [env]]
            [ring.adapter.jetty :as jetty]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.response :refer [render]]
            [clojure.pprint :as p]
            [ring.util.response :refer [response redirect content-type]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
            [clojure.java.io :as io])
  (:import (java.nio.file Files)
           (java.io File FileInputStream)))


(defn build-page-cache []
  (as-> (file-seq (File. (.getPath (clojure.java.io/resource "public/pages")))) $
        (filter #(.isFile %) $)
        (zipmap
          (map #(.getName %) $)
          (map #(Files/readAllBytes (.toPath %)) $))))


(def page-cache
  "Reads the gzipped blog content out of the resources
  dir and caches it in memory.

  (doesn't actually cache for dev modes)"
  (if (= (:stage env) "prod")
    (do (println "using fixed page cache")
        (constantly (build-page-cache)))
    (do (println "using live reload")
        build-page-cache)))




(defn retrieve-page
  "retrieve an already rendered page by its page slug/id"
  [id]
  {:pre [(string? id)]}
  (some-> (get (page-cache) id)
          io/input-stream
          response
          (update-in [:headers]
                     #(-> %
                          (assoc "Content-Encoding" "gzip")
                          (dissoc "Content-Length")))
          (content-type "text/html")))


(defn rss-feed
  "Fetches the pre-built RSS xml feed.

  This is generated offline via the tools in manage.clj."
  []
  (some-> (retrieve-page "rss.xml")
          (content-type "application/xml")))


(defroutes app-routes
   (routes
     (GET "/" [] (retrieve-page "home"))
     (GET "/about" [] (retrieve-page "about"))
     (GET "/patrons" [] (retrieve-page "patrons"))
     (GET "/rss.xml" [] (rss-feed ))
     (GET "/article/:slug" [slug] (retrieve-page slug))
     (route/resources "/")
     (route/not-found "Wha? Geit outta here")))


(def app
  (as-> app-routes $
        (wrap-cookies $)
        (wrap-keyword-params $)
        (wrap-params $)
        (wrap-json-params $)
        (wrap-multipart-params $)
        (wrap-session $)
        ))


(defn -main [& args]
  (println args)
  (jetty/run-jetty app {:port 8080}))
