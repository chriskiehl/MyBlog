(ns myblog.blog.controllers
  (:require [myblog.persistence.db :as db]
            [myblog.admin.articles :as articles]
            [myblog.admin.markdown :as markdown]
            [myblog.blog.views :as views]
            [hiccup.core :as hiccup]
            [ring.util.response :refer [response content-type]]
            [failjure.core :as f]))


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
  (views/about-page))
