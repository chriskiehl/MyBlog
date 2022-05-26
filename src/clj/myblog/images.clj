(ns myblog.images
  "Turns one image into images of many sizes!"
  (:require [myblog.types :as t]
            [cljs.spec.alpha :as s])
  (:import (javax.imageio ImageIO)
           (java.io File)
           (java.awt.image BufferedImage BufferedImageOp)
           (org.imgscalr Scalr Scalr$Method)
           (myblog.types ImageSrc)
           (java.awt Graphics2D Color)))


(defn rgba->rgb [^BufferedImage im]
  (let [[width height] [(.getWidth im) (.getHeight im)]
        jpg-friendly (BufferedImage. width height BufferedImage/TYPE_INT_RGB)
        gfx (.createGraphics jpg-friendly)]
    (.drawImage gfx im 0 0 Color/WHITE nil)
    (.dispose gfx)
    jpg-friendly))


(defn read-rgb-image
  "Reads a StaticImage and converts it from RGBA -> RGB
  so that it can be eventually stored as a jpeg. Unfortunately,
  BufferedImage & ImageIO aren't clever enough to handle this
  on their own thus treats the alpha channel incorrectly
  resulting in wonky colors in the output"
  [^String path]
  (rgba->rgb (ImageIO/read (File. path))))


(defn resize [^BufferedImage im, ^long target-width]
  (Scalr/resize im (Scalr$Method/ULTRA_QUALITY) target-width (make-array BufferedImageOp 0)))


(defn stepdown-plan
  "Creates 6 even step downs from the main width"
  [width]
  {:pre [(int? width)]}
  (let [largest-size (min 800 width)
        step (int (/ largest-size 6))
        steps (map #(int (- largest-size (* % step))) (range 6))]
    (filter #(> % 0) steps)))


(defn resized-filename
  "Strips the now irrelevant local file path info and
  rewrites the filename to one prefixed with the image size"
  [url size]
  (let [filename (.getName (File. ^String url))
        filename (.substring filename 0 (.lastIndexOf filename "."))
        filename (str filename ".jpeg")]
    (str "memory/" size filename)))


(defn generate-srcset
  "To avoid serving (and making users wait on) needlessly
  large images, we create a 'srcset' of smaller images from
  our full size one. Thus giving the browser ample options when
  choosing an image to load for the current screen size."
  [{:keys [url] :as content}]
  {:pre  [(t/>> :local/content content)]}
  (let [fullsize (read-rgb-image url)
        steps (stepdown-plan (.getWidth fullsize))
        filenames (map #(resized-filename url %) steps)
        resized (map #(resize fullsize %) steps)]
    (map #(ImageSrc. %1 %2) resized filenames)))
