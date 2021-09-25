(ns myblog.specs
  (:require [clojure.spec.alpha :as s]))


;; an 'enhanced' version of the standard markdown image tag
;; which contains additional srcset information. e.g.
;;    ![alt text@width1,widthN+ext](path/to/root/image)
(s/def :markdown/srcset string?)
