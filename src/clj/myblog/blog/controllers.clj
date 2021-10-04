(ns myblog.blog.controllers
  (:require [myblog.persistence.db :as db]
            [myblog.admin.articles :as articles]
            [myblog.admin.markdown :as markdown]
            [myblog.blog.views :as views]
            [hiccup.core :as hiccup]
            [ring.util.response :refer [response content-type header]]
            [failjure.core :as f]
            [clojure.java.io :as io])
  (:import (java.io FileInputStream File PipedInputStream PipedOutputStream Closeable ByteArrayOutputStream FileOutputStream ByteArrayInputStream)
           (java.util.zip GZIPOutputStream)
           (java.nio.charset StandardCharsets)
           (java.nio.file Files)
           (java.awt.image BufferedImage)
           (javax.imageio ImageIO)))



(defn image->stream [^BufferedImage image]
  (let [out (ByteArrayOutputStream.)]
    (ImageIO/write image "jpg" out)
    (ByteArrayInputStream. (.toByteArray out))))


(defn home-page [request]
  (let [articles (f/ok-> (db/list-published-articles)
                         articles/serialize-articles
                         (articles/sort-desc :published-on))]
    (if (f/failed? articles)
      (views/home-page [])
      (views/home-page articles))))


;(defn rss-feed [request]
;  (let [articles (f/ok-> (db/list-published-articles)
;                         articles/serialize-articles)]
;    (if (f/failed? articles)
;      (views/home-page [])
;      (-> (hiccup/html (views/rss-feed articles))
;          response
;          (content-type "application/xml")))))


;(defn article-page [slug]
;  (let [article (f/ok-> (db/get-published-article slug)
;                        articles/serialize
;                        markdown/preprocess-article)]
;    (if (f/failed? article)
;      (views/error-404-page)
;      (views/article-page article))))


(defn about-page [request]
  (let [about-me (slurp (clojure.java.io/resource "public/about/about.md"))]
    (views/about-page about-me)))


(defn load-cool-people []
  (-> "public/patrons/patrons.edn"
      (clojure.java.io/resource)
      (slurp )
      (read-string)))


(defn patrons [request]
  (let [cool-people (load-cool-people)]
    (views/patrons-page cool-people)))


(defn log [x]
  (println x)
  x)


;(defn compress [body]
;  (with-open [bytes-out (ByteArrayOutputStream.)
;              gzip-out  (GZIPOutputStream. bytes-out)
;              file-out  (FileOutputStream. "C:\\Users\\Chris\\Documents\\MyBlog\\resources\\public\\about\\foo.html.gz")]
;    (.write gg (.getBytes x StandardCharsets/UTF_8))
;    (.writeTo ss out))
;  (let [ss (ByteArrayOutputStream.)
;        gg (GZIPOutputStream. ss)
;        out (FileOutputStream. )]
;    (.write gg (.getBytes x StandardCharsets/UTF_8))
;    (.close gg)
;    ;(spit "C:\\Users\\Chris\\Documents\\MyBlog\\resources\\public\\about\\foo.html.gz" (String. (.toByteArray ss)))
;    (.writeTo ss out)
;    (.close out)
;    (.flush ss)
;    (.close ss))

(defn- compress-body
  [body]
  (let [p-in (PipedInputStream.)
        p-out (PipedOutputStream. p-in)]
    (future
      (with-open [out (GZIPOutputStream. p-out)]
        (if (seq? body)
          (doseq [string body] (io/copy (str string) out))
          (io/copy body out)))
      (when (instance? Closeable body)
        (.close ^Closeable body)))
    p-in))

(defn file-stream [x]
  (FileInputStream. (.getPath x)))


;(defn filter-non-matched [s]
;  (if ))

(def page-cache
  (as-> (file-seq (File. (.getPath (clojure.java.io/resource "public/pages")))) $
        (filter #(.isFile %) $)
        (zipmap
          (map #(.getName %) $)
          (map #(Files/readAllBytes (.toPath %)) $))))


(defn root-page [page-id]
  (println "page-id" page-id)
  (some-> (get page-cache page-id)
          io/input-stream
          response
          log
          (content-type "text/html")))

    ;(some-> (clojure.java.io/resource (str "public/pages/" page-id))
    ;    file-stream
    ;    response
    ;    log
    ;    (content-type "text/html")
    ;    ))

(defn rss-feed []
  (some-> (root-page "rss.xml")
          (content-type "application/xml")))

(defn article-page [slug]
  ;; 1. get ddb
  ;; 2. pluck out gzipped body
  ;; 3. return
  (some-> (clojure.java.io/resource (str "public/pages/articles/" slug ".html"))
          file-stream
          response
          log
          (content-type "text/html")))

