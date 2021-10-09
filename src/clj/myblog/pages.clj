(ns myblog.pages
  (:require [myblog.types :as t]
            [myblog.views :as views]))



(defn- load-metadata []
  (let [resource (clojure.java.io/resource "public/data/data.edn")]
    (read-string (slurp resource))))


(defn- load-popular []
  (let [resource (clojure.java.io/resource "public/data/popular.edn")]
    (read-string (slurp resource))))


(defn build-markdown-page
  "Builds an article/standalone page from the
  markdown source"
  [metadata body]
  (let [payload {:title (:title metadata) :published-body body}]
    (views/article-page payload)))


(defn build-home
  "Builds the home page based on the available articles
  and human supplied 'most popular' section"
  []
  {:pre []
   :post []}
  (let [meta (load-metadata)
        top-slugs (load-popular)
        popular (map #(get meta %) top-slugs)
        all-articles (->> (vals meta)
                          (filter #(= (:type %) :article))
                          (sort-by :published-on)
                          (reverse))]
    (when (some nil? popular)
      (throw (ex-info "ain't no article with that slug, friendo." {})))
    (views/home-page all-articles popular)))


(defn build-rss
  "Generates the RSS feed from the available articles"
  []
  (let [meta (load-metadata)
        all-articles (->> (vals meta)
                          (filter #(= (:type %) :article))
                          (sort-by :published-on)
                          (reverse))]
    (views/rss-feed all-articles)))