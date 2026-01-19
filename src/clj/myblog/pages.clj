(ns myblog.pages
  (:require [myblog.types :as t]
            [myblog.storage]
            [myblog.views :as views]))



(defn article
  "retrieve an already rendered page by its page slug/id"
  [db id]
  {:pre [(string? id)]}
  (some-> (get db id)
          views/article-page))


(defn home
  "Builds the home page based on the available articles
  and human supplied 'most popular' section"
  [db]
  {:pre [(t/>> :blog/db db)]
   :post []}
  (let [top-slugs (myblog.storage/load-popular)
        popular (map #(get db %) top-slugs)
        all-articles (->> (vals db)
                          (filter #(= (:type %) :article))
                          (sort-by :published-on)
                          (reverse))]
    (when (some nil? popular)
      (throw (ex-info "ain't no article with that slug, friendo." {})))
    (views/home-page all-articles popular)))


(defn rss-feed
  "Generates the RSS feed from the available articles"
  [db]
  (let [all-articles (->> (vals db)
                          (filter #(= (:type %) :article))
                          (sort-by :published-on)
                          (reverse))]
    (views/rss-feed all-articles)))


(defn robots [] "User-agent: *\nAllow: /")