(ns myblog.core
  (:gen-class)
  (:require [hiccup.core :refer :all]
            [amazonica.aws.s3 :as s3]
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
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [myblog.admin.controllers :as controllers]
            [myblog.blog.controllers :as blog-controllers]
            ))


(def backend (session-backend))

(extend-protocol cheshire.generate/JSONable
  java.time.LocalDateTime
  (to-json [datetime gen]
    (cheshire.generate/write-string gen (str datetime))))

(defn authenticated? [request]
  (contains? (:session request) :identity))


(defn page [] (str "
  <!DOCTYPE html>
    <html lang='en'>
      <head>
      <meta http-equiv='content-type' content='text/html;charset=UTF-8' />
      <meta name='viewport content='width=device-width' />

      <link rel='stylesheet' type='text/css' href='/css/primer.css' />
      <link rel='stylesheet' type='text/css' href='/css/highlight.css' />
      <link rel='stylesheet' type='text/css' href='/css/editor.css' />
      <title>Blogomatano - Editing</title>
      </head>
  <body>
  <div id='app'></div>
  <script src='/js/main.js' type='text/javascript'></script>
  <script>
    window.onload = function () {myblog.core.run();}
  </script>


  "
  "</body>
  </html>"))




(defn api-endpoint? [request]
  (.startsWith (:uri request) "/api/"))


(defn admin-url? [request]
  (.startsWith (:uri request) "/admin/"))


(defn api-auth-middleware [handler]
  (fn [request]
    (if (and (api-endpoint? request) (not (authenticated? request)))
      (assoc (response "Not Authorized") :status 401)
      (handler request))))


(defn admin-redirect-middleware
  [handler]
  (fn [request]
    (if (and (admin-url? request)
             (not (contains? (:session request) :identity)))
      (do (println "User not authenticated. Redirecting to /login/")
          (redirect "/login/"))
      (handler request))))


(defn logging-middleware [handler]
  (fn [request]
    (println (:session request))
    (println (authenticated? request))
    (handler request)))

(defroutes api-routes
  (GET "/api/articles/:id" [id :as request] (controllers/retrieve-article request id))
  (PUT "/api/articles/:id" [id :as request] (controllers/save-article request id))
  (GET "/api/articles/:id/history" [id :as request] (controllers/list-history request id))
  (PUT "/api/articles/:id/publish" [id :as request] (controllers/publish-article request id))
  (DELETE "/api/articles/:id/publish" [id :as request] (controllers/revoke-article request id))
  (PUT "/api/articles/:id/unlock" [id :as request] (controllers/enable-revisions request id))
  (DELETE "/api/articles/:id" [id :as request] (controllers/delete-article request id))
  (PUT "/api/upload" [] controllers/upload-image))


(defroutes app-routes
   (routes
     api-routes
     (GET  "/" [] blog-controllers/home-page)
     (GET  "/article/:slug" [slug] (blog-controllers/article-page slug))
     (GET  "/about" [] blog-controllers/about-page)
     (GET  "/rss.xml" [] blog-controllers/rss-feed)
     (GET  "/login" [] (controllers/login-page))
     (POST "/login" [] controllers/login-page)
     (POST "/logout" [] controllers/logout)
     (GET  "/admin" [] controllers/admin-dashboard)
     (POST "/admin/new" [] controllers/admin-create-article)
     (GET  "/admin/story/:id" [id] (page))
     (GET  "/admin/story/:id/history/:revision" [id revision :as request] (controllers/admin-history request id revision))
     (GET "/article/:id" [id] (html [:h3 "Gave me some of dat sweet" id]))
     (route/resources "/")
     (route/not-found "Wha? Geit outta here")))


(def app
  (as-> app-routes $
        (wrap-json-response $)
        (wrap-authorization $ backend)
        (wrap-authentication $ backend)
        (wrap-cookies $)
        (wrap-keyword-params $)
        (wrap-params $)
        (wrap-json-params $)
        (wrap-multipart-params $)
        ;(logging-middleware $)
        (admin-redirect-middleware $)
        (api-auth-middleware $)
        (wrap-session $)
        ))


(defn -main [& args]
  (println args)
  (jetty/run-jetty app {:port 8080}))
