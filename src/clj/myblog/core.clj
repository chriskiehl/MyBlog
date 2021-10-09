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
            [clojure.java.io :as io]))


(defn slurp-bytes
  "Slurp the bytes from a slurpable thing"
  [x]
  (with-open [out (java.io.ByteArrayOutputStream.)]
    (clojure.java.io/copy (clojure.java.io/input-stream x) out)
    (.toByteArray out)))


(defn load-resource [slug]
  (some-> (clojure.java.io/resource (str "public/pages/" slug))
          (slurp-bytes)))


(def load-page
  (if (= (:stage env) "prod")
    (memoize load-resource)
    load-resource))


(defn retrieve-page
  "retrieve an already rendered page by its page slug/id"
  [id]
  {:pre [(string? id)]}
  (some-> (load-page id)
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
  (jetty/run-jetty app {:port 8080 }))
