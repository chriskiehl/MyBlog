(ns myblog.manage
  "Primary controls for generating static content for the blog"
  (:require [myblog.types :as t]
            [myblog.obsidian :as obsidian]
            [myblog.pages :as pages]
            [clojure.java.io :as io])
  (:import (java.io ByteArrayOutputStream FileOutputStream)
           (java.util.zip GZIPOutputStream)
           (java.nio.charset StandardCharsets)))


(defn- write-gzip
  [path body]
  {:pre [(string? path) (string? body)]}
  (println path)
  (with-open [bytes-out (ByteArrayOutputStream.)
              gzip-out  (GZIPOutputStream. bytes-out)
              file-out  (FileOutputStream. path)]
    (do
      (.write gzip-out (.getBytes body StandardCharsets/UTF_8))
      (.finish gzip-out)
      (.write file-out (.toByteArray bytes-out)))))


(defn- save-page
  [slug html]
  (let [target-dir (clojure.java.io/resource "public/pages/")
        target-file (str (.getPath target-dir) slug)]
    (write-gzip  target-file html)))


(defn- save-metadata [metadata]
  (let [resource (clojure.java.io/resource "public/data/data.edn")
        data (read-string (slurp resource))]
    (->> (assoc data (:slug metadata) metadata)
         (spit resource))))


(defn- obsidian->blog-page
  [file-path]
  (let [{:keys [:meta/metadata :obsidian/body]} (obsidian/obsidian->html file-path)]
    {:metadata metadata
     :html (pages/build-markdown-page metadata body)}))


(defn add-page
  [file-path]
  {:pre [(t/>> :local/url file-path)]}
  (let [x (obsidian->blog-page file-path)]
    (save-metadata (:metadata x))
    (save-page (-> x :metadata :slug) (:html x))))


(defn remove-page [slug]
  (let [page (clojure.java.io/resource (str "public/pages/" slug))
        resource (clojure.java.io/resource "public/data/data.edn")
        data (read-string (slurp resource))]
    (io/delete-file (.getPath page))
    (->> (dissoc data slug)
         (spit resource))))


(defn update-roots []
  (do
    (save-page "home" (pages/build-home))
    (save-page "rss.xml" (pages/build-rss))))