(ns myblog.pasty
  (:require [ajax.core :refer [GET PUT DELETE]]
            [goog.string :as gstring]
            [goog.string.format]
            [myblog.util :as util]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.string :refer [includes?]]))


; TODO: figure out what to do when the
; browser doesn't strictly have focus during a drop event
; It causes the execCommands to fail, which means there's no
; target available / due to no selection, due to... etc..
; The event still fires, so maybe just insert an element at the
; top of the editor by hand..?


(defn is-pasting-text [data-transfer]
  (includes? (.-type data-transfer) "text/plain"))

(defn is-pasting-image [data-transfer]
  (includes? (.-type data-transfer) "image"))


(defn get-content [data-transfer-item]
  (.getData data-transfer-item "text/plain"))



(defn sign-url
  "Request a S3 signed URL from our API."
  [callback]
  (GET (str "/api/sign-url")
       :handler #(-> % keywordize-keys callback)
       :error-handler #(do (js/alert "unexpected failed while requesting signed URL")
                           (js/console.error (clj->js %)))))


(defn upload2
  "Request a S3 signed URL from our API."
  [file callback]
  (let [fd (doto (js/FormData.)
                 (.append "file" file (.-name file))
                 (.append "article" (util/get-path-id)))]
    (PUT (str "/api/upload")
         {:body fd
         :handler #(-> % keywordize-keys callback)
         :error-handler #(do (js/alert "unexpected failed while requesting signed URL")
                             (js/console.error (clj->js %)))})))


(defn set-placeholder!
  "Inject the markdown image placeholder text into the editor."
  [id]
  (let [template "<span id='%s'>![Uploading image.png…]()</span>"
        target (gstring/format template id)]
    (util/exec-insert-html target)))


(defn replace-placeholder!
  "replace the markdown placeholder text with the final image URL."
  [id new-text]
  (let [marker (.getElementById js/document id)]
    ; marker may at times be undefined due to not having
    ; proper focus of the browser window then drag/dropping (todo)
    (when marker
      (set! (.-innerText marker) new-text))))


(defn img-scrset [{:keys [main srcset] :as params}]
  (println "img-scrset " (clj->js params))
  (let [template "<img src='%s' srcset='%s' alt='A pretty cool image'>"]
    (gstring/format template main srcset)))


(defn paste-markdown-image
  "Handles the lifecycle of signing and uploading the image to s3
  while keeping the editor up-to-date with the status"
  [transfer-object]
  (let [id (random-uuid)
        file (.getAsFile transfer-object)]
    (set-placeholder! id)
    (upload2 file #(replace-placeholder! id (:srcset %)))))


(defn handle-clipboard-event
  "Dispatches to the appropriate handler given the contents of
  the TransferObject in the clipboard."
  [transfer-object]
  (let [transfer-items (array-seq (.-items transfer-object))
        image (first (filter is-pasting-image transfer-items))
        text (first (filter is-pasting-text transfer-items))]
    (when text
      (util/exec-insert-text (get-content transfer-object)))
    (when image
      (paste-markdown-image image))))


(defn handle-paste
  "handle pasting of of text, html, or image data."
  [event]
  (.preventDefault event)
  (handle-clipboard-event (.-clipboardData event)))


(defn handle-drop
  "Handle drop events for images."
  [event]
  (.preventDefault event)
  (handle-clipboard-event (.-dataTransfer event)))