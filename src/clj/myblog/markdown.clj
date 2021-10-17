(ns myblog.markdown
  (:require [myblog.types :as t]
            [hiccup.core :as hiccup]
            [cljs.spec.alpha :as s])
  (:import (myblog.types ImageSrcSet RemoteSrc Gif RemoteSrcSet Mp4 Other Mp3 Webm)))



(defn srcset-entry [{:keys [url size] :as data}]
  {:pre [(instance? RemoteSrc data)]}
  (format "%s %sw" url size))


(defn srcset [remotes]
 (clojure.string/join ", " (map srcset-entry remotes)))


(defmulti to-html
          "generates relevant HTML in response to the
          incoming type."
          type)


(defmethod to-html RemoteSrcSet [^RemoteSrcSet content]
  {:post [(string? %)]}
  (let [images (:images content)]
    (hiccup/html
      [:img {:alt (:alt content)
             :class (:classes content)
             :src (:url (first images))
             :srcset (srcset images)}])))


(defmethod to-html Gif [^Gif content]
  {:post [(string? %)]}
  (hiccup/html [:img {:src (:url content)
                      :alt (:alt content)}]))


(defmethod to-html Mp4 [^Mp4 content]
  (hiccup/html
    [:video {:autoplay "false"
             :controls "true"
             :style "max-width: 100%"}
     [:source {:src (:url content)
               :type "video/mp4"}]]))


(defmethod to-html Webm [^Webm content]
  (hiccup/html
    [:video {:autoplay "false"
             :controls "true"
             :style "max-width: 100%"}
     [:source {:src (:url content)
               :type "video/webm"}]]))


(defmethod to-html Mp3 [^Mp3 content]
  (hiccup/html
    [:audio {:autoplay "false"
             :controls "true"
             :style "max-width: 100%"}
     [:source {:src (:url content)
               :type "audio/mp3"}]]))


(defmethod to-html Other [^Other content]
  (hiccup/html
    [:a {:href (:url content)
         :target "_blank"}
     (if (clojure.string/blank? (:alt content))
       (:url content)
       (:alt content))]))