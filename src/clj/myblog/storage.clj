(ns myblog.storage
  (:require [environ.core :refer [env]]
            [amazonica.aws.s3 :as s3]
            [myblog.types :as t]
            [environ.core :refer [env]])
  (:import (java.io InputStream)))


(def creds
  {:access-key (:aws-access-key env)
   :secret-key (:aws-secret-key env)
   :endpoint (:aws-endpoint env)})


(defn put-public-object
  ""
  [{:keys [key content metadata] :as request}]
  {:pre [(t/>> string? key)
         (instance? InputStream content)
         (map? (or metadata {}))]}
  (s3/put-object
    (assoc creds :endpoint "us-east-1")
    :region "us-east-1"
    :bucket-name "awsblogstore"
    :key key
    :input-stream content
    :canned-acl "public-read"
    :metadata (or metadata {})))


(defn load-popular []
  (let [resource (clojure.java.io/resource "public/data/popular.edn")]
    (read-string (slurp resource))))


(defn- encode [db]
  (letfn [(encode [s]
            (.encodeToString (java.util.Base64/getEncoder) (.getBytes s "UTF-8")))]
    (reduce-kv #(assoc %1 %2 (update %3 :static-content encode)) {} db)))


(defn- decode [db]
  (letfn [(decode [to-decode]
            (String. (.decode (java.util.Base64/getDecoder) to-decode)"UTF-8"))]
    (reduce-kv #(assoc %1 %2 (update %3 :static-content decode)) {} db)))


(defn save-db
  "Persists the 'DB' whole-sale. HTML content is b64 encoded
  mostly due to superstition"
  [db]
  (let [path (clojure.java.io/resource "public/data/db.edn")]
    (spit path (encode db))))


(defn load-db
  "Loads our 'database' (which is just a .edn file of
  all the articles and stuff) into memory"
  []
  (let [resource (clojure.java.io/resource "public/data/db.edn")]
    (decode (read-string (slurp resource)))))


