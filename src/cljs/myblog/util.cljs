(ns myblog.util)


(defn get-path-id
  "Manually pluck the article ID from the URL since we're
  not using the router"
  []
  (-> js/window (.-location) (.-pathname)
      (clojure.string/split "/")
      last))


(defn exec-insert-html [html]
  (.execCommand js/document "insertHTML" false html))


(defn exec-insert-text [text]
  (.execCommand js/document "insertText" false text))



(defn debouncer
  "Delays invocation of `f` until `amount` of milliseconds
  has elaspsed without `f` being called again"
  [f amount]
  (let [run-token (atom 0)]
    (fn [& args]
      (let [this-token (swap! run-token inc)]
        (js/setTimeout (fn []
                         (when (= this-token @run-token)
                           (apply f args)))
                       amount)))))


(defn set-inner-html! [target-id html]
  (set! (.-innerHTML (.getElementById js/document target-id)) html))

(defn set-inner-text! [target-id text]
  (set! (.-innerText (.getElementById js/document target-id)) text))



(defn drop-blank-vals
  "Drops any keys from a map which have empty/blank/nil values"
  [coll]
  (reduce-kv
    #(if-not (clojure.string/blank? %3)
       (assoc %1 %2 %3)
       %1)
    {}
    coll))
