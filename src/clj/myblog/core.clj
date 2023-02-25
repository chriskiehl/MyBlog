(ns myblog.core
  (:gen-class)
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :refer :all]
            [myblog.pages :as pages]
            [myblog.storage :as storage]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.util.response :refer [content-type response]]))


(def db (storage/load-db))


(defn retrieve-article
  "retrieve an already rendered page by its page slug/id"
  [db id]
   {:pre [(string? id)]}
   (some-> (pages/article db id)
           response
           (content-type "text/html")))


(defn rss-feed
  "Fetches the pre-built RSS xml feed.

  This is generated offline via the tools in manage.clj."
  [db]
  (some-> (pages/rss-feed db)
          response
          (content-type "application/xml")))


(defroutes app-routes
   (routes
     (GET "/" [] (pages/home db))
     (GET "/about" [] (retrieve-article db "about"))
     (GET "/help-plz" [] (retrieve-article db "halp-plz"))
     (GET "/rss.xml" [] (rss-feed db))
     (GET "/article/:slug" [slug] (retrieve-article db slug))
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
