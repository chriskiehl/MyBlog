(ns obsidian
  (:import (java.io File)))




(defn index-files []
  (as-> (file-seq (File. "C:\\Users\\Chris\\Dropbox\\notes\\")) $
        (zipmap (map #(.getName %) $)
                (map #(.getPath %) $))))


(defn extract-images
  "Extract the image names from the Obsidian
  flavored markdown.

  ![[filename.png]] => ['![[filename.png]]', 'filename.png']
  "
  [content]
  (->> (re-seq #"!\[\[.+\]\]" content)
       (map #(re-find #"!\[\[(.+)\]\]" %))))


(defn obsidian->markdown [content index]
  (let [images (extract-images content)
        template "![%s](%s)"]
    (for [[obsidian-text filename] images]
      [obsidian-text (format template filename (index filename))])))


(defn convert
  "Converts the Obsidian markdown flavor to a one
  understandable by other systems"
  [path]
  (let [content (slurp path)
        index (index-files)
        conversions (obsidian->markdown content index)]
    (reduce
      (fn [s [to-replace replacement]]
        (clojure.string/replace s to-replace replacement))
      content
      conversions)))