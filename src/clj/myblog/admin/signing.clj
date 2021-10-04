(ns myblog.admin.signing
  (:require [environ.core :refer [env]]
            [amazonica.aws.s3 :as s3]
            [failjure.core :as f]
            [clojure.java.io :as io])
  (:import (com.amazonaws.auth BasicAWSCredentials AWSStaticCredentialsProvider)))


(def creds (BasicAWSCredentials. (:aws-access-key env) (:aws-secret-key env)))



(defn put-object [key filepath content-type]
  (with-open [stream (io/input-stream filepath)]
    (s3/put-object
      creds
      :bucket-name "awsblogstore"
      :key key
      :input-stream stream
      :canned-acl "public-read"
      :metadata {:content-type content-type
                 :cache-control "max-age=31557600"})))


(let [x (clojure.java.io/resource "public/im.jpeg")
      im (javax.imageio.ImageIO/read x)
      new (Scalr/resize im (Scalr$Method/QUALITY) 490 (make-array BufferedImageOp 0))]
  (s3/put-object
    {:access-key "AKIAJK5GGA7PBQ6GTJUQ"
     :secret-key "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"
     :endpoint "us-east-1"}
    :region "us-east-1"
    :bucket-name "awsblogstore"
    :key "temp/temp.jpg"
    :input-stream (image->stream new)
    :canned-acl "public-read"
    :metadata {:content-type "image/jpg"
               :cache-control "max-age=31557600"}))
