(ns myblog.obsidian
  "I've gone full ham on my obsession with Obsidian. As such, my entire
  blog is now driven by it. This handles the processing of an Obsidian file
  and turning all of its embedded content into remote content on S3 and
  rewriting the static obsidian links as type relevant html."
  (:require [clojure.spec.alpha :as s]
            [ring.util.mime-type :as mime]
            [environ.core :refer [env]]
            [myblog.types :as t]
            [myblog.util :refer [->stream]]
            [myblog.storage :as storage]
            [markdown.core :as md]
            [myblog.images :as images]
            [myblog.markdown :refer [to-html]])
  (:import (java.io File)
           (myblog.types StaticImage Gif Other ImageSrcSet Mp4 Mp3 ImageSrc RemoteSrc RemoteSrcSet Webm)
           (java.awt.image BufferedImage)))


(def s3-host (:s3-host env))
(def vault-url (:vault-url env))

(println s3-host)

(defn filetype
  "Extract the extension/type from the file's path.
  e.g. foo.png -> png"
  [filepath]
  {:pre [(t/>> :local/url filepath)]
   :post [(t/>> string? %)]}
  (.toLowerCase (last (clojure.string/split filepath #"\."))))


(defn classify
  "Typing the relevant file formats so that they can
  be treated uniformly while still carrying different information
  and being dispatched on"
  [{:keys [:local/url :obsidian/link-text :obsidian/alt-text :obsidian/classes] :as derefed}]
  {:pre [(t/>> :obsidian/link derefed)]
   :post [(t/>> :local/content %)]}
  (println derefed)
  (case (filetype url)
    "jpg"  (StaticImage. link-text url classes alt-text)
    "jpeg" (StaticImage. link-text url classes alt-text)
    "png"  (StaticImage. link-text url classes alt-text)
    "gif"  (Gif. link-text url classes alt-text)
    "mp4"  (Mp4. link-text url classes alt-text)
    "webm" (Webm. link-text url classes alt-text)
    "mp3"  (Mp3. link-text url classes alt-text)
    (Other. link-text url classes alt-text)))


(defn index-files
  "Index all files in the Obsidian locker producing a
  map from filename -> filepath"
  [vault-root]
  {:pre [(t/>> :local/url vault-root) (.exists (File. ^String vault-root))]
   :post [(t/>> :obsidian/index %)]}
  (as-> (file-seq (File. ^String vault-root)) $
        (zipmap (map #(.getName %) $)
                (map #(.getPath %) $))))


(defn parse-link
  "Parses an Obsidian link of the form
    ![[filename.ext]]
    ![[filename.ext|alt text here]]
    ![[filename.ext|css classes here | alt text here]]

  into its requisite pieces
    ['filename.ext' 'css classes here' 'alt text here']"
  [link]
  {:pre [(t/>> :obsidian/link-text link)]}
  (let [regex #"!\[\[(.*)\]\]"
        [_ contents] (re-matches regex link)
        name #(.getName (File. %))
        tokens (clojure.string/split contents #"\|")]
    (case (count tokens)
      1 {:filename (name (first tokens)) :classes "" :alt ""}
      2 {:filename (name (first tokens)) :classes "" :alt (second tokens)}
      3 {:filename (name (first tokens)) :classes (second tokens) :alt (nth tokens 2)})))


(defn extract-embedded-links
  "Collects any linked content (images, gifs, pdfs, etc) from
  the markdown source. Anything of the form ![[blah.ext]]. Regular
  page links are excluded."
  [src]
  {:pre  [(t/>> :obsidian/source src)]
   :post [(t/>> :obsidian/links %)]}
  (->> (re-seq #"!\[\[.+\]\]" src)
       (map
         #(let [{:keys [alt filename classes]} (parse-link %)]
            {:obsidian/link-text %
             :obsidian/classes classes
             :local/filename filename
             :obsidian/alt-text alt}))))


(defn extract-linked-content
  "Extract and dereference any Obsidian content links
  with their fully qualified filesystem paths."
  [index src]
  {:pre  [(t/>> :obsidian/index index) (t/>> :obsidian/source src)]
   :post [(t/>> (s/coll-of :local/content) %)]}
  ;; just 'splodes if it can't deref any of the files.
  ;; probably a sign of my Obsidian source being setup funky
  (->> (extract-embedded-links src)
       (map
         #(let [path (get index (:local/filename %))
                derefed (assoc % :local/url path)]
            (when (nil? path)
              (throw (ex-info "unable to deref" %)))
            (classify derefed)))))


(defn pre-process
  ""
  [content]
  {:pre [(t/>> :local/content content)]
   :post [(t/>> :processed/content %)]}
  (if (instance? StaticImage content)
    (ImageSrcSet. (:link content) (:alt content) (:classes content) (images/generate-srcset content))
    content))


(defn build-key
  ([url] (build-key "" url))
  ([prefix, ^String url]
   (let [name (.getName (File. url))
         url-safe (clojure.string/replace name #"\s" "-")]
     (str "content/" prefix url-safe))))


(defn build-meta [url]
  {:content-type (mime/ext-mime-type url)
   :cache-control "max-age=3600"})


(defn put-object [url body]
  (let [request {:key (build-key url)
                 :metadata (build-meta url)
                 :content (->stream body)}]
    (println (storage/put-public-object request))
    (str s3-host (:key request))))


(defmulti upload type)

(defmethod upload ImageSrcSet [content]
  {:pre [(t/>> :processed/content content)]}
  (let [images (:images content)
        sizes (map #(.getWidth (:image %)) images)
        remote-urls (->> images
                         (map #(future (put-object (:url %) (:image %))))
                         (into []) ;; force eval of the futures
                         (map deref))]
    (RemoteSrcSet.
        (:link content)
        (:alt content)
        (:classes content)
        (map #(RemoteSrc. %1 %2) remote-urls sizes))))


(defmethod upload :default [content]
  {:pre [(t/>> :processed/content content)]}
  (let [result (put-object (:url content) (:url content))]
    (assoc content :url result)))


(defn rewrite-links
  [src remotes]
  {:pre [(t/>> :obsidian/body src) (t/>> (s/coll-of :remote/content) remotes)]
   :post [(t/>> :obsidian/source %)]}
  (reduce
    (fn [updated-src content]
      (clojure.string/replace updated-src (:link content) (to-html content)))
    src
    remotes))


(defn parse-source
  "split the obsidian source into its requisite
  parts of the metadata header and body "
  [src]
  {:pre [(t/>> :obsidian/source src)]
   :post [(t/>> :obsidian/parsed %)]}
  (let [[_ raw-metadata body] (re-matches #"(?s)^\`\`\`edn(.*?)\`\`\`(.*)" src)]
    (when (nil? raw-metadata)
      (throw (ex-info "unable to parse metadata. Missing EDN block!" {})))
    (let [metadata (read-string raw-metadata)]
      (s/assert :meta/raw-meta metadata)
      {:raw-meta metadata :body body})))



(defn title-image
  [metadata remotes]
  (let [images (first (filter #(= (:link %) (:title-image metadata)) remotes))]
    (when (nil? images)
      (throw (ex-info "Something went wrong! Couldn't find the remote title image(s)!" remotes)))
    (-> (assoc metadata :title-images images)
        (dissoc :title-image))))


(defn obsidian->html
  "Reads the obsidian source file, extracts its embedded content
   and uploads it to the C L O U D and finally, replaces the local file
   links the now remote content"
  [file-path]
  {:pre [(t/>> :local/url file-path)]
   :post [(t/>> :obsidian/processed %)]}
  (let [index (index-files vault-url)
        raw-obsidian-src (slurp file-path)
        {:keys [raw-meta body]} (parse-source raw-obsidian-src)
        _ (println "SOURCE")
        _ (println body)
        content-links (extract-linked-content index raw-obsidian-src)
        remote-content (map (comp upload pre-process) content-links)
        processed-body (md/md-to-html-string (rewrite-links body remote-content))
        final-metadata (title-image raw-meta remote-content)]
    (println "Processed Metadata:")
    (clojure.pprint/pprint final-metadata)
    (println "\nPreprocessed Page Body:\n")
    (println processed-body)
    {:meta/metadata final-metadata
     :obsidian/body processed-body}))


