(ns myblog.admin.markdown
  (:require [hiccup.core :as hiccup]))


(defn key-val-parser
  "Makes a simple single-line key/val parsing function

  Format assumptions:
    * keys and values separated by colons
    * pairs separated by newlines

    Regex breakdown:

    (?m) ; multiline mode
    (?:^target-key:) ; negative lookahead skipping key section
    (.*(?=\\n)) ; captures remaining chars until the next newline
  "
  [regex]
  (fn [input]
    (->> input
         (re-find regex)
         last
         clojure.string/trim)))


(defn parse-widths
  "Parse the source-set widths from the markdown srcset string

  (extract-widths '![image@1024,819,614,409,204+png](path/to/thing)')
  => ('1024' '819' '614' '409' '204')
  "
  [markdown-srcset]
  (let [widths-regex #"((?!=@)[\d,]+(?=\+))"]
    (as-> markdown-srcset $
          (re-find widths-regex $)
          (last $)
          (clojure.string/split $ #",")
          (map clojure.string/trim $))))

(defn parse-alt-text
  "Parse the alt text from the beginning from the srcset string.

  (parse-alt-text '![I am some alt text!@{widths}] etc...')
  => 'I am some alt text!'
  "
  [markdown-srcset]
  (let [regex #"(?:!\[)([\w\W]+?(?=@))"]
    (as-> markdown-srcset $
          (re-find regex $)
          (last $)
          (clojure.string/trim $))))


(defn parse-content-root
  "
  Extract the content root from the srcset string.

  (parse-content-root '![image@1024,819+png](path/to/thing)')
  => 'path/to/thing'
  "
  [markdown-srcset]
  (let [content-regex #"(?:!\[[\w\W]+?@[\d,\.+\w]+\]\()(.*(?=\)))"]
    (as-> markdown-srcset $
          (re-find content-regex $)
          (last $)
          (clojure.string/trim $))))


(defn parse-extension
  ""
  [markdown-srcset]
  (let [ext-regex #"(?:!\[[\w\W]+?@[\d,]+\+)(\w+(?=]))"]
    (as-> markdown-srcset $
          (re-find ext-regex $)
          (last $)
          (clojure.string/trim $))))


(defn srcset->map
  "Parses the custom markdown srcset format to produce
  fully qualified URLs paired with their widths.

  Regex are all structures such that the relevant capture
  group is _always_ the last group. Thus `last` can be safely called.

  Thus, content-root is matched in two groups:
    1. the first is everything up to and included in the opening (
    2. the remaining content up to but Excluding the closing )

  ![alt@widths](content-root)

  Width extraction wraps everything in an additional capture group
  to follow this 'final group is relevant rule'.
  "
  [srcset-str]
  (let [widths (parse-widths srcset-str)
        content-root (parse-content-root srcset-str)
        ext (parse-extension srcset-str)
        ; normal src url format
        src-template "%s-%spx.%s"
        ; srcset url + width format
        srcset-template "%s-%spx.%s %sw"
        to-srcset-str #(format srcset-template content-root %1 ext %1)]
    {:src (format src-template content-root (first widths) ext)
     :srcset (clojure.string/join ", " (map to-srcset-str widths))
     :alt (parse-alt-text srcset-str)
     :content-root content-root
     :widths widths}))



(defn parse-md-srcset
  "Parses the custom markdown srcset format to produce
  fully qualified URLs paired with their widths.

  Regex are all structures such that the relevant capture
  group is _always_ the last group. Thus `last` can be safely called.

  Thus, content-root is matched in two groups:
    1. the first is everything up to and included in the opening (
    2. the remaining content up to but Excluding the closing )

  ![alt@widths](content-root)

  Width extraction wraps everything in an additional capture group
  to follow this 'final group is relevant rule'.
  "
  [srcset-str]
  (let [widths (parse-widths srcset-str)
        content-root (parse-content-root srcset-str)
        ext (parse-extension srcset-str)
        template "%s-%spx.%s"]
    (map vector
         (map #(format template content-root % ext) widths)
         (map #(str % "w") widths))))


(defn image-tag [scrset-map]
  (hiccup/html [:img (select-keys scrset-map [:alt :src :srcset])]))



(defn markdown-srcset
  "So, turns out having a giant srcset statement in the middle of the
  doc is crazy ugly. So, instead of doing that, we make a custom
  markdown element that specifies the available sizes and then we
  resolve the full img srcset tag at read time. doh.

  Syntax is the standard markdown image block, but with with the addition
  of comma separated width values

  Schema:
  ![{alt}@{widths}+{ext}]({s3dir}/{name})

  Example:
  ![image@600,200+jpeg](http://example.com/path/myfile)

  Unpacks to:
  http://example.com/path/myfile-600px.jpeg
  http://example.com/path/myfile-200px.jpeg
  "
  [work-order]
  (format "![image@%s+%s](%s)"
          (clojure.string/join "," (map :target-width work-order))
          (-> work-order first :extension)
          (-> work-order first :s3-content-path)))


(def srcset-block-regex #"!\[[\w\W]+?@[\d,]+\+\w+\]\(.*?\)")


(def parse-title-key
  "parse the `title-image:` key from the document body"
  (key-val-parser #"(?m)(?:^title-image:)(.*(?=\n))"))


(defn replace-srcset-markdown [string]
  (let [matches (re-seq srcset-block-regex string)
        extracted-sources (map (comp image-tag srcset->map) matches)
        replacements (map vector matches extracted-sources)]
    (reduce
      (fn [s [original replacement]]
        (clojure.string/replace s original replacement))
      string
      replacements)))



(defn remove-metadata-section
  "Remove the [metadata] section entirely so that it
  isn't rendered"
  [body]
  (let [title-image (-> body
                        parse-title-key
                        replace-srcset-markdown)
        meta-section #"\[metadata\]:[\s\S]*?\[end-metadata\]"]
    (clojure.string/replace body meta-section title-image)))


(defn preprocess-article [{:keys [published-body] :as article}]
  (let [cleaned-body (-> published-body
                         remove-metadata-section
                         replace-srcset-markdown)]
    (-> article
        (assoc :published-body cleaned-body))))


