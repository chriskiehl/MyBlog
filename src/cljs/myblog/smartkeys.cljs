(ns myblog.smartkeys
  (:require [myblog.util :as util]
            [goog.string :as gstring]
            goog.string.format))

;
;(defn silence-shortcut-combos []
;  (.addEventListener js/window "keydown" (fn [x]
;                                           (let [key (.toLowerCase (.-key x))
;                                                 ctrlkey-down (.-ctrlKey x)]
;                                             (when (and (= "d" key) ctrlkey-down)
;                                               (.preventDefault x))))))
;
;
;
;
;
;
;(def ^:private wrappable
;  "Smart Keys 'surround' behavior is applied to
;  these characters"
;  {"\"" {:open \" :close \"}
;   "'"  {:open \' :close \'}
;   "["  {:open \[ :close \]}
;   "("  {:open \( :close \)}
;   "{"  {:open \{ :close \}}
;   "~"  {:open \~ :close \~}
;   "`"  {:open \` :close \`} })
;
;
;(def ^:private pairable
;  "Smart Keys 'pair' behavior is applied to
;  these characters"
;  {"["  {:open \[ :close \]}
;   "("  {:open \( :close \)}
;   "{"  {:open \{ :close \}}
;   "\"" {:open \~ :close \~}})
;
;
;
;(defn is-collapsed?
;  "Returns whether or not the selection at the time of this event
;  is 'collapsed' or, in non-selection lingo, whether the selection
;  is a highlighted piece of text, or just a cursor in between some text"
;  [event]
;  (-> js/window
;      .getSelection
;      (.getRangeAt 0)
;      (.-collapsed)))
;
;
;(defn- span-tag [x id]
;  (gstring/format "<span id='%s'>%s</span>" id x))
;
;
;(defn get-selected-text
;  "Get whatever text (if any) is currently selected in the document"
;  []
;  (-> js/window .getSelection .toString))
;
;
;(defn reselect-wrapped-text
;  "Reselect the text node which was just wrapped accounting for the
;  offsets created by the wrapping"
;  [id]
;  (let [selection (.getSelection js/window)
;        range (.getRangeAt selection 0)
;        elm (.getElementById js/document id)
;        firstchild (.-firstChild elm)
;        lastchild (.-lastChild elm)
;        text (.-data lastchild)]
;    ;; offset + 1 to account for the wrapper we just added
;    (.setStart range firstchild 1)
;    ;; offset - 1 to account for the wrapper we just added
;    (.setEnd range lastchild (dec (count text)))
;    (.addRange selection range)))
;
;
;(defn text->html
;  "Convert the next to basic HTML by replacing
;  newlines with <br> tags"
;  [text]
;  (clojure.string/replace text "\n" "<br>"))
;
;
;
;(defn wrap-text
;  "Stuff the text in between the supplied open/close characters"
;  [text open close]
;  (gstring/format "%s%s%s" open text close))
;
;
;(defn siblings-while
;  "traverse via nextSibling until there are no
;  more siblings or the predicate is satisfied."
;  [element pred]
;  (if (or (pred element) (nil? element))
;    []
;    (cons element (siblings-while (.-nextSibling element) pred))))
;
;
;(defn select-between [start-id end-id]
;  (let [selection (-> js/window .getSelection)
;        range (.getRangeAt selection 0)
;        start-elm (.getElementById js/document start-id)
;        end-elm (.getElementById js/document end-id)]
;    (if (or (nil? start-elm) (nil? end-elm))
;      (js/console.warn "unable to locate requested selection: " start-id end-id)
;      (let [dom-nodes (siblings-while start-elm #(= (.-id %) (.-id end-elm)))]
;        ))))
;
;
;(defn build-wrapped-payload [event]
;  (let [{:keys [open close]} (wrappable (.-key event))
;        id (random-uuid)
;        wrapped-html (-> (get-selected-text)
;                         (wrap-text open close)
;                         text->html
;                         (span-tag id))]
;    {:id id
;     :html wrapped-html}))
;
;
;(defn surround-text
;  "Wrap the selected keys with the supplied 'smart keys'
;  character and reselect it again for further wrapping"
;  [event]
;  (js/console.info "event" event)
;  (let [{:keys [open close]} (wrappable (.-key event))
;        start-id (random-uuid)
;        wrapped-html (-> (get-selected-text)
;                         (wrap-text open close)
;                         text->html
;                         (span-tag start-id))]
;    (util/exec-insert-html wrapped-html)
;    (reselect-wrapped-text start-id)
;    ))
;
;
;
;(defn insert-pair-bracket [event]
;  (let [selection (.getSelection js/window)
;        id (random-uuid)
;        html-brackets (span-tag "[]" id)]
;    (js/console.info id)
;    (util/exec-insert-html html-brackets)
;    (let [elm (.getElementById js/document id)
;          range (-> js/window .getSelection (.getRangeAt 0))]
;      (.setStart range (.-firstChild elm) 1)
;      (.setEnd range (.-firstChild elm) 1)
;      (.collapse range)
;      (.addRange selection range))))
;
;
;(def xf
;  "Channel transducer which strips out all
;  events which aren't of interest to wrap-text behaviors"
;  (comp
;    (filter (comp wrappable util/event->key))
;    (filter (complement is-collapsed?))
;    (map util/prevent-default)))
;
;(def pair-bracket-xf
;  ""
;  (comp
;    (filter (comp #{"[" "{" "\""} util/event->key))
;    (filter is-collapsed?)
;    (map util/prevent-default)))
;
;
