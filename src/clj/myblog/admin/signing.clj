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

