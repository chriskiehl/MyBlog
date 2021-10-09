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
  "https://awsblogstore.s3.amazonaws.com/temp/temp.jpg"
  (s3/put-object
    (assoc creds :endpoint "us-east-1")
    :region "us-east-1"
    :bucket-name "awsblogstore"
    :key key
    :input-stream content
    :canned-acl "public-read"
    :metadata (or metadata {})))

