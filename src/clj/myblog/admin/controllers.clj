(ns myblog.admin.controllers
  (:require [myblog.admin.views :as templates]
            [myblog.persistence.db :as db]
            [myblog.admin.articles :as articles]
            [myblog.admin.markdown :as markdown]
            [myblog.admin.images :as images]
            [environ.core :refer [env]]
            [ring.util.response :refer [not-found response header redirect content-type]]
            [failjure.core :as f]
            [clojure.data.json :as json]
            [myblog.admin.signing :as s3])
  (:import (java.io PrintWriter)
           (clojure.lang ExceptionInfo)))

(defn stringify-localdate [date ^PrintWriter out]
  (.print out (str "\"" date "\"")))

(extend java.time.LocalDateTime json/JSONWriter
  {:-write stringify-localdate})


(def authdata {(:username env) (:password env)})


(println authdata)

(defn bad-response
  "Returns a skeletal Ring response with the given body, status
  of 400, and no headers."
  [body]
  {:status  400
   :headers {}
   :body    body})


(defn login-page
  "Check request username and password against authdata
  username and passwords. On successful match, the session
  is updated with user info.

  Copied from Buddyauth."
  ([]
   (-> (templates/login-page)
       response
       (header "Content-Type" "text/html")))
  ([{:keys [params session] :as request}]
  (let [username (:username params)
        password (:password params)
        found-password (get authdata username)]
    (if (and found-password (= found-password password))
      (let [updated-session (assoc session :identity (keyword username))]
        (-> (redirect "/admin/")
            (assoc :session updated-session)))
      (response (templates/login-page {:error true}))))))


(defn logout [{:keys [session] :as request}]
  (let [updated-session (dissoc session :identity)]
    (-> (redirect "/")
        (assoc :session updated-session))))


(defn admin-dashboard
  "The main admin dashboard page."
  [request]
  (let [result (f/ok-> (db/list-articles)
                articles/serialize-articles
                templates/admin-dashboard
                response)]
    (if (f/failed? result)
      (do (println result)
          (bad-response (f/message result)))
      result)))


(defn admin-history
  "A simple plain text dump of the selected revision"
  [request id revision]
  (let [result (db/get-revision id revision)]
    (if (f/failed? result)
      (response "Not found, pal.")
      (-> result
          articles/serialize
          (:body result)
          response
          (header "Content-Type" "text/plain")))))


(defn admin-create-article
  "Process the incoming form request and create a new article"
  [request]
  (let [on-success (fn [{:keys [id] :as article}]
                   (redirect (str "/admin/story/" id)))
        result (f/ok-> (articles/empty-article)
                        articles/stamp-metadata
                        db/conditionally-save-article
                        db/index-latest
                        on-success)]
    (if (f/failed? result)
      (str (f/message result))
      result)))


;;
;; API Controllers
;;

(defn format-errors
  "quick and dirty generic api error handler."
  [error]
  (let [data (.getData error)
        resp (bad-response {:message (.getMessage error)})]
    (if (= (:type data) :not-found)
      (assoc resp :status 404)
      resp)))


;(defn sign-url [request]
;  (let [id (str (java.util.UUID/randomUUID))
;        result (s3/sign-url id)]
;    (if (f/failed? result)
;      (format-errors (f/message result))
;      (response result))))




(defn upload-image [{:keys [params] :as request}]
  (let [result (f/ok-> (images/load-image (:file params))
                       (assoc :article-id (:article params))
                       images/save-locally
                       images/build-work-order
                       images/generate-images!
                       images/upload-images!
                       images/cleanup!
                       markdown/markdown-srcset)]
    (if (f/failed? result)
      (format-errors (f/message result))
      (response {:srcset result}))))




(defn retrieve-article
  "get dat article"
  [request id]
  (let [result (f/ok-> (db/get-article id)
                       articles/serialize
                       (dissoc :query-key))]
    (if (f/failed? result)
      (format-errors (f/message result))
      (response result))))


(defn list-history
  "get dat history"
  [request id]
  (let [result (f/ok-> (db/list-history id)
                       articles/serialize-articles
                       articles/sort-by-date
                       reverse)]
    (if (f/failed? result)
      (format-errors (f/message result))
      (response result))))


(defn save-article
  "save dat article"
  [request id]
  (let [result (f/ok-> (:params request)
                       db/db-deser
                       articles/rewrite-image-fields
                       articles/stamp-metadata
                       db/save-article
                       articles/serialize)]
    (if (f/failed? result)
      (do ; todo: logger
          (println result)
          (bad-response {:message (-> result f/message)}))
      (response result))))


(defn publish-article
  "Publish it"
  [request id]
  (let [article (db/get-article id)
        result (f/ok-> article
                       articles/publish-article
                       db/save-article)]
    (if (f/failed? result)
      (format-errors (f/message result))
      (response (-> result
                    articles/serialize)))))


(defn enable-revisions [request id]
  (let [article (db/get-article id)
        result (f/ok-> article
                       articles/enable-revisions
                       db/save-article)]
    (if (f/failed? result)
      (format-errors (f/message result))
      (response (-> article
                    articles/enable-revisions
                    articles/serialize)))))


(defn revoke-article [request id]
  (let [article (db/get-article id)
        result (f/ok-> article
                       articles/revoke-article
                       db/save-article)]
    (if (f/failed? result)
      (format-errors (f/message result))
      (response result))))


(defn delete-article [request id]
  (let [article (db/get-article id)
        result (f/ok-> article
                       articles/delete-article
                       db/save-article)]
    (if (f/failed? result)
      (format-errors (f/message result))
      (response (articles/delete-article result)))))
