(ns myblog.core
  (:gen-class)
  (:require [hiccup.core :refer :all]
            [ring.adapter.jetty :as jetty]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.response :refer [render]]
            [ring.util.response :refer [response redirect content-type]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
            [myblog.blog.controllers :as blog-controllers]
            ))


(extend-protocol cheshire.generate/JSONable
  java.time.LocalDateTime
  (to-json [datetime gen]
    (cheshire.generate/write-string gen (str datetime))))


(defroutes app-routes
   (routes
     (GET "/" [] blog-controllers/home-page)
     (GET "/about" [] (blog-controllers/root-page "about"))
     (GET "/patrons" [] (blog-controllers/root-page "patrons"))
     (GET "/rss.xml" [] (blog-controllers/rss-feed))
     (GET "/article/:slug" [slug] (blog-controllers/article-page slug))
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
