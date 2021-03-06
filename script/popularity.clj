(ns popularity
  (:require [clojure.data.json :as json]
            [myblog.persistence.db :as db]
            [myblog.admin.controllers :as controller]
            [failjure.core :as f]))


;; parse the nginx logs and sum up the number of page views
;; for each article
;; nginx logs look like this:
;; 173.56.55.31 - - [09/Feb/2019:03:04:19 +0000] \"GET /article/parallelism-in-one-line/ HTTP/1.1\" 301 194 \"-\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/605.1.15 (KHTML, like Gecko)\"
;;
;; any line with GET /article/:slug is matched and counted
;; {slug1 20, slug2 123, slugN 2000}


(defn readlines-gzip [path process-lines]
  (with-open [lines (as-> path $
                          (clojure.java.io/input-stream $)
                          (java.util.zip.GZIPInputStream. $)
                          (java.io.InputStreamReader. $)
                          (java.io.BufferedReader. $))]
    (process-lines (line-seq lines))))



(defn readlines [path process-lines]
  (with-open [lines (as-> path $
                          (clojure.java.io/input-stream $)
                          (java.io.InputStreamReader. $)
                          (java.io.BufferedReader. $))]
    (process-lines (line-seq lines))))


(defn lineseq-contents [path f]
  (if (clojure.string/ends-with? path "gz")
    (readlines-gzip path f)
    (readlines path f)))


(defn list-access-files []
  (as-> "/etc/nginx/logs/" $
        (java.io.File. $)
        (.listFiles $)
        (map str $)
        (filter #(clojure.string/includes? % "access.log") $)))


(defn process-pageviews
  "parse all the nginx logs and produce a frequencies map
  from article-slug -> number of views"
  [lines]
  (as-> lines $
        (map #(second (re-find #"/article/([\w-]+)" %)) $)
        (filter (complement nil?) $)
        (frequencies $)))


(defn compute-pageviews
  []
  (println "will process: " (list-access-files))
  (->> (list-access-files)
       (map #(lineseq-contents % process-pageviews))
       (apply merge-with +)))


(defn stamp-pageviews
  "Stamp the updated page view numbers on the published articles"
  [views-map]
  (doseq [[slug views] views-map]

    (let [article (f/ok-> (db/get-published-article slug)
                          (#(controller/retrieve-article {} (:id %)))
                          :body
                          (assoc :views views))]
      (when (not (f/failed? article))
        (println "\nUpdating view for article: " slug)
        (controller/save-article
          {:params (json/read-str (json/write-str article) :key-fn keyword)}
          "")))))


(when (= (second *command-line-args*) "actually-run-this")
  (stamp-pageviews (compute-pageviews)))
