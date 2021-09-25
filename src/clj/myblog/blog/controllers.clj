(ns myblog.blog.controllers
  (:require [myblog.persistence.db :as db]
            [myblog.admin.articles :as articles]
            [myblog.admin.markdown :as markdown]
            [myblog.blog.views :as views]
            [hiccup.core :as hiccup]
            [ring.util.response :refer [response content-type header]]
            [failjure.core :as f])
  (:import (java.io FileInputStream File)))


(defn home-page [request]
  (let [articles (f/ok-> (db/list-published-articles)
                         articles/serialize-articles
                         (articles/sort-desc :published-on))]
    (if (f/failed? articles)
      (views/home-page [])
      (views/home-page articles))))


(defn rss-feed [request]
  (let [articles (f/ok-> (db/list-published-articles)
                         articles/serialize-articles)]
    (if (f/failed? articles)
      (views/home-page [])
      (-> (hiccup/html (views/rss-feed articles))
          response
          (content-type "application/xml")))))


(defn article-page [slug]
  (let [article (f/ok-> (db/get-published-article slug)
                        articles/serialize
                        markdown/preprocess-article)]
    (if (f/failed? article)
      (views/error-404-page)
      (views/article-page article))))


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

(defn local [req]
  (-> (FileInputStream. (File. (.getPath (clojure.java.io/resource "public/about/foo.html.gz"))))
      response
      (content-type "text/html")
      (header "Content-Encoding" "gzip")
      log
      ))