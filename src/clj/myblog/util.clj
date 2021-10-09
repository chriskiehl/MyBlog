(ns myblog.util
  (:require [clojure.java.io :as io])
  (:import (java.io ByteArrayInputStream ByteArrayOutputStream)
           (javax.imageio ImageIO ImageWriter ImageWriteParam IIOImage)
           (java.awt.image BufferedImage)))



(defmulti ->stream type)

(defmethod ->stream BufferedImage [^BufferedImage im]
  (println "BufferedImage streamin' and screamin'")
  (let [out (ByteArrayOutputStream.)
        ^ImageWriter writer (.next (ImageIO/getImageWritersByFormatName "jpg"))
        ^ImageWriteParam params (.getDefaultWriteParam writer)
        out2 (ImageIO/createImageOutputStream out)]
    (.setCompressionMode params ImageWriteParam/MODE_EXPLICIT)
    (.setCompressionQuality params 0.8)
    (.setOutput writer out2)
    (.write writer nil (IIOImage. im nil nil) params)
    (ByteArrayInputStream. (.toByteArray out))))


(defmethod ->stream :default [path]
  (println "default" path)
  (io/input-stream path))
