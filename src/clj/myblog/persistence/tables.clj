(ns myblog.persistence.tables
  (:require [amazonica.aws.dynamodbv2 :as ddb2]
            [environ.core :refer [env]]))



(defn create-articles-table
  "Articles Table - Primary storage for article drafts."
  [creds table-name]
  (ddb2/create-table
    creds
    :table-name table-name
    :key-schema [;; `id` of an article/post.
                 {:attribute-name :id :key-type "HASH"}
                 ;; `version` field follows the 'Active Version'
                 ;; strategy. New writes are append-only via the
                 ;; timestamp. The latest version is always copied
                 ;; to timestamp = 0 and queried via GSI for list views.
                 {:attribute-name :version :key-type "RANGE"}]
    :attribute-definitions [{:attribute-name :id :attribute-type "S"}
                            {:attribute-name :version :attribute-type "S"}
                            ;; `query-key` is a sparse GSI attr only
                            ;; present on the timestamp=0 records. It is the
                            ;; concatenation of 0 + timestamp
                            ;; (e.g. 0:::2018-01-01...), which allows lexical
                            ;; sorting when reading from DynamoDB.
                            ;; Additionally, GSI Overloading is used here to
                            ;; enable querying Published posts from the same
                            ;; index. They are written under SK=PUBLISHED, rather
                            ;; than SK=0
                            ;; meaning
                            ;; All published:
                            ;;      kind=article
                            ;;      query-key=starts_with("PUBLISHED")
                            ;; All Latest versions:
                            ;;      kind=article
                            ;;      query-key=starts_with('0')
                            {:attribute-name :query-key :attribute-type "S"}
                            ;;; GSI PK target (fixed query key "article")
                            {:attribute-name :kind :attribute-type "S"}
                            ;{:attribute-name :query-slug :attribute-type "S"}
                            ]
    :provisioned-throughput {:read-capacity-units 5 :write-capacity-units 5}
    :global-secondary-indexes
    [{:index-name :latest-version-index
      :key-schema [{:attribute-name :kind :key-type "HASH"}
                   {:attribute-name :query-key :key-type "RANGE"}]
      :projection {:projection-type "ALL"}
      :provisioned-throughput {:read-capacity-units  5 :write-capacity-units 5}}
     ;;; an index with the slug as the primary key to allow fast
     ;;; frontend lookups. This key will be written EXCLUSIVELY on the active
     ;;; version
     ;{:index-name :slug-identifier
     ; :key-schema [{:attribute-name :version :key-type "HASH"}
     ;              {:attribute-name :query-slug :key-type "RANGE"}]
     ; :projection {:projection-type "ALL"}
     ; :provisioned-throughput {:read-capacity-units  5 :write-capacity-units 5}}
     ]
    ))

