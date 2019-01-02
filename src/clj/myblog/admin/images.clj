(ns myblog.admin.images
  (:require [clojure.java.io :as io]
            [clojure.string :refer [join]]
            [clojure.java.shell :as shell]
            [failjure.core :as f]
            [myblog.admin.signing :as s3]
            [myblog.admin.util :as util]))


(defn load-image
  "Load the temp-file as an ImageIO object and compute some
  basic metadata for downstream systems"
  [{:keys [filename tempfile] :as file-upload}]
  (let [image (javax.imageio.ImageIO/read tempfile)
        target-filename (str (util/short-uuid) "-" filename)
        target-dir "resources/public/images/"
        local-path (str target-dir target-filename)]
    (merge file-upload {:local-path local-path
                        :filename target-filename
                        :name (util/drop-ext target-filename)
                        :extension (util/extension target-filename)
                        :target-dir target-dir
                        :image image
                        :original-filename filename
                        :width (.getWidth image)
                        :height (.getHeight image)})))


(defn save-locally
  "Save the uploaded tempfile file locally to the filesystem"
  [{:keys [local-path image tempfile extension] :as enhanced-file-info}]
  (println enhanced-file-info)
  (try
    (clojure.java.io/copy
      tempfile
      (java.io.File. local-path))
    (assoc enhanced-file-info :saved-locally true)
    (catch Exception e
      (f/fail (ex-info (.getMessage e) {})))))


(defn gen-thumbnail-sizes
  "Generates a sequence of ever smaller widths
  for thumbnail images based on the number of steps

  Example: 5 equally spaced thumbnail sizes
    (gen-thumbnail-sizes 1024 5)
    => (1024 819 614 409 204 0)
  "
  [image-width steps]
  (let [step-size (/ image-width steps)]
    (reductions
      #(int (- %1 %2))
      image-width
      ;; the number of steps - 1 to prevent producing
      ;; a target width of 0
      (repeatedly (dec steps) (constantly step-size)))))


(defn build-command
  "Build the command line string which
  will be fed to image image magik to generate the thumbnails "
  [input-path output-path width]
  ["convert" "-thumbnail" (str width) input-path output-path])


(defn build-work-order
  "Create a bundle of metadata describing the thumb-nailing
  work that will be done on the command line. A lot of metadata
  is produced as part of this so that downstream systems can
  produce the final markdown packed formats without needing
  to reparse out the info

  name: myfile
  filename: myfile-200w.jpeg
  s3dir: http://example.com/path/
  s3-content-path http://example.com/path/myfile
  s3key: http://example.com/path/myfile-200w.jpeg

  ![image@600,200+jpeg](http://example.com/path/myfile)
  ![{alt}@{widths}+{ext}]({s3dir}/{name})

  http://example.com/path/myfile-600px.jpeg
  http://example.com/path/myfile-200px.jpeg
  "
  [{:keys [article-id target-dir name local-path width content-type] :as image-data}]
  (map (fn [size]
         (let [public-root "https://s3.amazonaws.com/awsblogstore/"
               filename (format "%s-%spx.%s" name size "jpeg")
               output-path (.getPath (io/file target-dir filename))
               s3-key (.getPath (io/file "articles" article-id filename))
               s3-content-path (str public-root (.getPath (io/file "articles" article-id name)))
               public-url (str public-root s3-key)]
           {:name name
            :filename filename
            :extension "jpeg"
            :s3-content-path s3-content-path
            :s3key s3-key
            :public-url public-url
            :input-path local-path
            :output-path output-path
            :content-type content-type
            :target-width size
            :command (build-command local-path output-path size)}))
       (gen-thumbnail-sizes width 6)))


(defn generate-images!
  "Execute the command line instructions stored in the work order."
  [work-order]
  (let [results (map #(apply shell/sh (:command %)) work-order)
        errors (filter #(not (nil? (:error %))) results)]
    (if (empty? errors)
      (map #(assoc % :status :success) work-order)
      (ex-info (join (map :error errors)) {}))))



(defn upload-images!
  "Upload all the thumbnails to S3"
  [work-order]
  (->> work-order
       (map (fn [{:keys [s3key output-path content-type] :as work-item}]
              (future
                (let [result (s3/put-object s3key output-path content-type)]
                  (assoc work-item :s3-result result)))))
       (map deref)))


(defn cleanup!
  "Remove all the temporarily created image files
  from the local disk"
  [work-order]
  (doseq [{:keys [output-path]} work-order]
    (when-not (.delete (java.io.File. output-path))
      ;; ultimately whatevs. Log and move on
      (println "Failed while trying to delete " output-path)))
  work-order)


(defn srcset-info
  "Build an html srcset string from all our image sizes"
  [work-order]
  {:main (-> work-order first :public-url)
   :srcset (->> work-order
                (map #(let [{:keys [public-url target-width]} %]
                        (str (format "%s %sw" public-url target-width))))
                (clojure.string/join ", "))})


