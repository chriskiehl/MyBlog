(ns myblog.parser
  (:require [myblog.markdown :as markdown]))

(comment "Utils for parsing the metadata section of the document.")


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


(def parse-title-key
  "parse the `title-image:` key from the document body"
  (key-val-parser #"(?m)(?:^title-image:)(.*(?=\n))"))

(def parse-slug
  "parse the `slug:` key from the document body"
  (key-val-parser #"(?m)(?:^slug:)(.*(?=\n))"))

(def parse-description
  "parse the `description:` key from the document body"
  (key-val-parser #"(?m)(?:^description:)(.*(?=\n))"))

(def parse-publish-date
  "parse the `publish-date:` key from the document body"
  (key-val-parser #"(?m)(?:^publish-date:)(.*(?=\n))"))

(defn srcset-block? [string]
  (re-matches
    #"!\[[\w\W]+?@[\d,]+\+\w+\]\(.*?\)"
    (clojure.string/trim string)))

(defn parse-metadata
  "Extract all fields from the metadata section of the document"
  [body]
  (let [parse-fns [parse-title-key parse-slug
                   parse-description parse-publish-date]]
    (zipmap
      [:title-image :slug :description :publish-override]
      (map #(% body) parse-fns))))



(defn remove-metadata-section
  "Remove the [metadata] section entirely so that it
  isn't rendered"
  [body]
  (let [title-image (try
                      (-> body
                          parse-title-key
                          markdown/replace-srcset-markdown)
                      (catch js/Error e
                        ;; if we don't find the title image
                        ;; no biggie; we just replace with
                        ;; an empty string until it becomes available.
                        ""))
        meta-section #"\[metadata\]:[\s\S]*?\[end-metadata\]"]
    (clojure.string/replace body meta-section title-image)))

