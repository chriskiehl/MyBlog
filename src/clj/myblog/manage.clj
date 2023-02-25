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


(defn add-page
  "Adds a new statically rendered page into our 'db'"
  [file-path]
  {:pre [(t/>> :local/url file-path)]}
  (let [db (myblog.storage/load-db)
        page (obsidian/obsidian->blog file-path)]
    (myblog.storage/save-db (assoc db (:slug page) page))))


(defn remove-page
  "Removes an existing page from our 'db'"
  [slug]
  (let [db (myblog.storage/load-db)
        page (clojure.java.io/resource (str "public/pages/" slug))]
    (io/delete-file (.getPath page))
    (->> (dissoc db slug)
         (myblog.storage/save-db))))


(defn reload-all
  "Here for the times when I need to re-render
  all the static content."
  [published-path]
  (doseq [f (->> (file-seq (clojure.java.io/file published-path))
                 (filter #(.isFile %))
                 ;; the theater article is a javascript rendered page, so there's
                 ;; no edn blob or markdown for us to process. Thus: excluded.
                 (filter #(not (clojure.string/includes? (.getAbsolutePath %) "Theater"))))]
    (println (.getAbsolutePath f))
    (add-page (.getAbsolutePath f))))
