(ns myblog.persistence.db
  (:require [amazonica.aws.dynamodbv2 :as ddb2]
            [amazonica.aws.s3 :as s3]
            [environ.core :refer [env]]
            [failjure.core :as f]
            [amazonica.aws.s3 :as s3]
            [myblog.admin.util :as util]
            [clojure.string :refer [blank?]])
  (:import (com.amazonaws AmazonServiceException)
           (java.time LocalDateTime)))

(def *table-name* (:table-name env))

(def *creds*
  (util/drop-blank-vals
    {:access-key (:aws-access-key env)
     :secret-key (:aws-secret-key env)
     :endpoint (:aws-endpoint env)
     :region (:aws-region env)}))




(defn db-ser
  "stringify the dates before passing off to dynamo."
  [article]
  (reduce-kv (fn [m k v]
               (if (instance? java.time.LocalDateTime v)
                 (assoc m k (str v))
                 (assoc m k v)))
             {}
             article))


(defn db-deser
  "parse the stringified the dates coming from dynamo"
  [article]
  (let [date-keys [:written-on :created-on :published-on]]
    (reduce (fn [acc target-key]
              (if (contains? acc target-key)
                (update acc target-key #(LocalDateTime/parse %))
                acc))
            article
            date-keys)))


(defn flag-empty-query-as-not-found
  "We query + limit 1 to pop off the latest available item.
  However, queries do not through NotFound exceptions even on a Hash miss.
  So we fake it to keep a sane API"
  [results]
  (if (or (= 0 (:count results)) (empty? results))
    (f/fail (ex-info "NotFound" {:type :not-found}))
    results))


(defn flag-deleted-as-not-found
  ""
  [article]
  (if (= (:state article) "deleted")
    (f/fail (ex-info "NotFound" {:type :not-found}))
    article))


(defn prep-signed-data
  "Put the URL returned from a signing request into some structured format"
  [id url]
    {:signed-url (str url)
     :public-url (str "https://s3.amazonaws.com/awsblogstore/" id)})



(defn- persist-article [article]
  (ddb2/put-item *creds*
                 :table-name *table-name*
                 :return-consumed-capacity "TOTAL"
                 :return-item-collection-metrics "SIZE"
                 :item (db-ser article))
  article)


(defn- conditionally-persist-article [article]
  ;; I can't figure out how to merge these &args
  ;; style functions... so copy/pasta-ing
  (ddb2/put-item *creds*
                 :table-name *table-name*
                 :return-consumed-capacity "TOTAL"
                 :return-item-collection-metrics "SIZE"
                 :condition-expression "attribute_not_exists(id) and attribute_not_exists(version)"
                 :item (db-ser article))
  article)


(defn index-latest [{:keys [created-on slug] :as article}]
  (let [item (merge article {:version "0" :query-key (str 0 ":::" slug)})]
    (persist-article item)))


(defn save-article [article]
  (let [result (f/try* (persist-article article))]
    (if (f/failed? result)
      result
      ;; hand back the original article
      ;; on success for ease of chaining
      (do (future (index-latest article))
          article))))

(defn conditionally-save-article
  "Save the article with a db condition that the ID must
  not already exist (just as a sanity measure against collisions)"
  [article]
  (let [result (f/try* (conditionally-persist-article article))]
    (if (f/failed? result)
      result
      ;; hand back the original article
      ;; on success for ease of chaining
      (do (future (index-latest article))
          article))))


(defn- query-article [id]
  (ddb2/query *creds*
              :table-name *table-name*
              :limit 1
              :scan-index-forward false
              :key-conditions
              {:id {:attribute-value-list [id]
                    :comparison-operator  "EQ"}}))

(defn- query-article-by-slug [slug]
  (ddb2/query *creds*
              :table-name *table-name*
              :limit 1
              :index-name :latest-version-index
              :scan-index-forward false
              :filter-expression "#s = :pub OR #s = :rev"
              :expression-attribute-names {"#s" "state"}
              :expression-attribute-values {":pub" "published"
                                            ":rev" "revising"}
              :key-conditions
              {:kind {:attribute-value-list ["article"]
                      :comparison-operator "EQ"}
               :query-key {:attribute-value-list [(str "0:::" slug)]
                         :comparison-operator  "EQ"}}))


(defn serialize-article
  "Apply the readtime niceties like word-count
  human edit dates, etc..."
  [{:keys [id words written-on created-on] :as article}]
  (merge article {
                  ;:written-on (str written-on)
                  ;:created-on (str written-on)

                  :creation-date-str (util/calendar created-on)
                  :wordcount (format "%s word%s" words (if (not= words 1) "s" ""))
                  :last-edited (util/calendar written-on)
                  :url       (format "/api/articles/%s" id)
                  :edit-url  (format "/admin/story/%s" id)}))


(defn list-articles []
  (println "using creds: " *creds*)
  (println "using table: " *table-name*)
  (f/ok-> (f/try* (ddb2/query *creds*
                              :table-name *table-name*
                              :scan-index-forward false
                              :index-name :latest-version-index
                              :key-conditions
                              {:kind {:attribute-value-list ["article"]
                                      :comparison-operator "EQ"}
                               :query-key {:attribute-value-list ["0"]
                                         :comparison-operator "GE"}}
                              :projection-expression
                              "id, #wc, #w, #c, words, #s, #v, #t"
                              :filter-expression "not #s = :ddd"
                              :expression-attribute-names {"#s" "state"
                                                           "#w" "written-on"
                                                           "#wc" "wordcount"
                                                           "#c" "created-on"
                                                           "#v" "version"
                                                           "#t" "title"
                                                           }
                              :expression-attribute-values {":ddd" "deleted"}))
          :items
          (#(map db-deser %))))



(defn list-published-articles []
  (f/ok-> (f/try* (ddb2/query *creds*
                              :table-name *table-name*
                              :scan-index-forward false
                              :index-name :latest-version-index
                              :key-conditions
                              {:kind {:attribute-value-list ["article"]
                                      :comparison-operator "EQ"}
                               :query-key {:attribute-value-list ["0"]
                                           :comparison-operator "GE"}}
                              :filter-expression "#s = :pub OR #s = :rev"
                              :projection-expression
                              "#qk, id, #wc, #w, #c, words, #s, #v, #t, #ti, #tis, #tia, #p, #sl, #ds, #vs"
                              :expression-attribute-names {"#s" "state"
                                                           "#w" "written-on"
                                                           "#p" "published-on"
                                                           "#wc" "wordcount"
                                                           "#c" "created-on"
                                                           "#v" "version"
                                                           "#t" "title"
                                                           "#ti" "title-image"
                                                           "#tis" "title-image-srcset"
                                                           "#tia" "title-image-alt"
                                                           "#sl" "slug"
                                                           "#ds" "description"
                                                           "#qk" "query-key"
                                                           "#vs" "views"}
                              :expression-attribute-values {":pub" "published"
                                                            ":rev" "revising"}
                              ))
          :items
          (#(map db-deser %))))


(defn list-history
  "List all past revisions for the given article ID"
  [id]
  (f/ok-> (f/try*
            (ddb2/query
              *creds*
              :table-name *table-name*
              :key-conditions
              {:id {:attribute-value-list [id]
                    :comparison-operator "EQ"}
               ;; we ignore anything beginning with 0
               ;; as those will be duplicate entries due
               ;; to the active version index.
               :version {:attribute-value-list ["0"]
                         :comparison-operator "GT"}}
              :projection-expression
              "id, wordcount, #w, #c, words, #s, #v"
              :expression-attribute-names {"#w" "written-on"
                                           "#c" "created-on"
                                           "#s" "state"
                                           "#v" "version"}))
          :items
          (#(map db-deser %))))






(defn get-article
  "Try to load an article from storage."
  [id]
  (f/ok-> (util/try* (query-article id))
          flag-empty-query-as-not-found
          :items
          first
          flag-deleted-as-not-found
          db-deser))


(defn get-published-article
  "Retrieve a published article by its url slug"
  [slug]
  (f/ok-> (util/try* (query-article-by-slug slug))
          flag-empty-query-as-not-found
          :items
          first
          flag-deleted-as-not-found
          db-deser))

(defn get-revision [id revision]
  (f/ok-> (util/try* (ddb2/get-item
                       *creds*
                       :table-name *table-name*
                       :key {:id      id
                             :version revision}))
          flag-empty-query-as-not-found
          :item
          db-deser))

