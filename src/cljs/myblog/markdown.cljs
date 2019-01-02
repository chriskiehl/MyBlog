(ns myblog.markdown
  (:require [goog.string :as gstring]
            [goog.string.format]))

; I don't know how to share code between clj/cljs.. so.. more copy/paste..


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
         (map #(gstring/format template content-root % ext) widths)
         (map #(str % "w") widths))))


(defn image-tag [url-width-pairs]
  (let [join clojure.string/join
        [url _] (first url-width-pairs)
        srcset-urls (join ", " (map #(join " " %) url-width-pairs))
        template "<img src='%s' srcset='%s'>"]
    (gstring/format template url srcset-urls)))



(defn replace-srcset-markdown [string]
  (let [srcset-regex #"!\[[\w\W]+?@[\d,]+\+\w+\]\(.*?\)"
        matches (re-seq srcset-regex string)
        extracted-sources (map (comp image-tag parse-md-srcset) matches)
        replacements (map vector matches extracted-sources)]
    (reduce
      (fn [s [original replacement]]
        (clojure.string/replace s original replacement))
      string
      replacements)))


