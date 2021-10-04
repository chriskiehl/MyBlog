(ns myblog.types
  (:require [clojure.spec.alpha :as s]))

(def not-blank? (complement clojure.string/blank?))
(def non-empty-str? (s/and string? not-blank?))
(defn iso-date-str? [^String s] (re-matches #"\d{4}-\d{2}-\d{2}" s))
(defn obsidian-link? [^String s] (re-matches #"!\[\[.+\]\]" s))
;(defn image-type? [^String obsidian-link]
;  (let [s (clojure.string/lower-case obsidian-link?)]))



(comment "

data RemoteContent
  = ImageSrcSet (Array Url)
  | Gif Url
  | Mp3 Url
  | Mp4 Url
  | Pdf Url

type LocalFile = String
type ObsidianLink = String
type ObjectIndex = Map Filename Path

data RawMetadata
data ObsidianMeta = ArticleMeta

type DereferencedLink = {
  obsidianLink :: ObsidianLinkText,
  filename :: String,
  localpath :: LocalFilePath
}

index :: LocalFilePath -> Effect ObjectIndex
loadFile :: LocalFilePath -> Effect ObsidianSource
extractLinks :: ObsidianSource -> Array ObsidianLink
derefLinks :: Array ObsidianLink -> ObjectIndex -> Map ObsidianLink LocalFilePath
generateRemoteContent :: LocalFilePath -> Effect RemoteContent
  | Gif -> preprocessor -> upload -> RemoteContent
  | Png/Jpeg/jpeg -> preprocessor -> upload -> RemoteContent
  | _ -> preprocessor -> upload -> RemoteContent

f :: Map ObsidianLink LocalFilePath -> Map ObsidianLink RemoteContent

-- pre-process to convert the Obsidian tags to HTML targeting the now remote content
f :: ObsidianSource -> Map ObsidianLink RemoteContent -> ObsidianSource

-- grab the final transformed metadata
f :: ObsidianSource -> Metadata
f :: MarkdownProcessor -> ObsidianSource -> Html

-- write the gzipped file to disk
-- write the metadata to disk
f :: Metadata -> Html -> Effect Unit
f :: Metadata -> Effect Unit

preprocess :: PageSource -> Map ObsidianLink Srcset -> PreprocessedMarkdown

markdown :: PreprocessedMarkdown -> FinalHtml

gzip :: Metadata -> FinalHtml -> Effect Unit
")

(s/check-asserts true)

(defn >> [spec x] (s/assert spec x))




(comment "I don't know how to do it in Clojure
but these should be treated as a co-product. e.g

  data Content
    = ImageSrcSet
    | Gif
    | Mp4
    ...
")
(defrecord ImageSrc [image url])
(defrecord RemoteSrc [url size])
(defrecord ImageSrcSet [link alt images])
(defrecord RemoteSrcSet [link alt images])
(defrecord StaticImage [link url alt])
(defrecord Gif [link url alt])
(defrecord Mp4 [link url alt])
(defrecord Mp3 [link url alt])
(defrecord Other [link url alt])

(defn instance-of [& args]
  (fn [x] (some identity (map #(instance? % x) args))))

(s/def ::url string?)

(s/def :obsidian/source string?)
(s/def :obsidian/body string?)
(s/def :obsidian/parsed (s/keys :req-un [:meta/raw-meta :obsidian/body]))
(s/def :obsidian/processed (s/keys :req [:meta/metadata :obsidian/body]))

(s/def :obsidian/alt-text string?)
(s/def :obsidian/link-text #(re-matches #"!\[\[.+\]\]" %))
(s/def :obsidian/link
  (s/keys :req [:obsidian/link-text :local/filename :obsidian/alt-text]))
(s/def :obsidian/derefed-link
  (s/keys :req [:obsidian/link-text :local/filename :local/content]))

(s/def :local/content (instance-of StaticImage Gif Mp4 Mp3 Other))
(s/def :processed/content (instance-of ImageSrcSet Gif Mp4 Mp3 Other))
(s/def :remote/content (instance-of RemoteSrcSet Gif Mp4 Mp3 Other))

(s/def :obsidian/derefed-links (s/coll-of :obsidian/derefed-link))

(s/def :obsidian/links (s/coll-of :obsidian/link))

(s/def :file/path string?)
(s/def :file/name string?)
(s/def ::file-path string?)
(s/def ::s3-url string?)

(s/def :local/url string?)

(s/def :local/gif ::url)
(s/def :local/static-image ::url)
(s/def :local/mp4 ::url)
(s/def :local/mp3 ::url)
(s/def :local/pdf ::url)

(s/def :remote/gif ::url)
(s/def :remote/image-srcset ::url)
(s/def :remote/mp4 ::url)
(s/def :remote/mp3 ::url)
(s/def :remote/pdf ::url)


(s/def :obsidian/index (s/and not-empty (s/map-of :file/name :file/path)))
(s/def :obsidian/replacements (s/map-of :obsidian/link :file/path))

(s/def :meta/slug #(re-matches #"[\w-]*" %))
(s/def :meta/title non-empty-str?)
(s/def :meta/title-image :obsidian/link-text)
(s/def :meta/title-images :remote/content)
(s/def :meta/description non-empty-str?)
(s/def :meta/published-on #(re-matches #"\d{4}-\d{2}-\d{2}" %))


(s/def :meta/raw-meta (s/keys :req-un [:meta/slug
                                       :meta/title
                                       :meta/title-image
                                       :meta/description
                                       :meta/published-on]))


(s/def :meta/metadata (s/keys :req-un [:meta/slug
                                       :meta/title
                                       :meta/title-images
                                       :meta/description
                                       :meta/published-on]))




