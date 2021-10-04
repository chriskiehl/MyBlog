(ns myblog.storage
  (:require [environ.core :refer [env]]
            [amazonica.aws.s3 :as s3]
            [clojure.java.io :as io]
            [myblog.types :as t])
  (:import (myblog.types ImageSrcSet)
           (java.awt.image BufferedImage)
           (java.io ByteArrayOutputStream ByteArrayInputStream InputStream)
           (javax.imageio ImageIO)))






(defn put-public-object
  ""
  [{:keys [key content metadata] :as request}]
  {:pre [(t/>> string? key)
         (instance? InputStream content)
         (map? (or metadata {}))]}
  "https://awsblogstore.s3.amazonaws.com/temp/temp.jpg"
  (s3/put-object
    {:access-key "lol!"
     :secret-key "Whoops!"
     :endpoint "us-east-1"}
    :region "us-east-1"
    :bucket-name "awsblogstore"
    :key key
    :input-stream content
    :canned-acl "public-read"
    :metadata (or metadata {})))

