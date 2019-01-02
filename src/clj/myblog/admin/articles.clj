(ns myblog.admin.articles
  (:require [myblog.admin.util :as util]
            [myblog.admin.markdown :as markdown]
            [clojure.string :refer [join]]
            [failjure.core :as f])
  (:import (java.time LocalDateTime)))


(def baseline-metadata "
[metadata]:
title-image:
slug:
description:
publish-date:
[end-metadata]
")



(defn empty-article
  "Create an empty article"
  []
  (let [now (LocalDateTime/now)]
    {:id         (util/short-uuid)
     :title      "Untitled"
     :created-on now
     :written-on now
     :body       (clojure.string/trim baseline-metadata)
     :words      0
     :state      :draft
     :version    (str now)
     :kind       "article"}))


(defn sort-by-date
  "Another threading macro play-nice utility function"
  [articles]
  (sort-by :written-on articles))


(defn sort-desc [articles key]
  (reverse (sort-by key articles)))


(defn rewrite-image-fields
  "The client sends a srcset markdown block as the title image"
  [{:keys [title-image] :as article}]
  (if-let [srcset-map (and title-image
                          (re-matches markdown/srcset-block-regex title-image)
                          (markdown/srcset->map title-image))]
    (-> article
        (assoc :title-image (:src srcset-map))
        (assoc :title-image-srcset (:srcset srcset-map))
        (assoc :title-image-alt (:alt srcset-map)))
    (dissoc article :title-image)))


(defn stamp-metadata
  "Markup the article with some simple metadata
  words counts, write date, etc.."
  [{:keys [body] :as article}]
  (let [now (LocalDateTime/now)]
    (merge article {:words (-> body (clojure.string/split #"\s") count)
                    :written-on now
                    :version (str now)})))


(defn serialize
  "Apply the read time niceties like friendly word counts
  human readable edit dates, relevant urls, etc..."
  [{:keys [id words written-on version created-on] :as article}]
  (merge
    article
    {:creation-date-str (util/calendar created-on)
     :wordcount         (format "%s word%s" words (if (not= words 1) "s" ""))
     :last-edited       (util/calendar written-on)
     :url               (format "/api/articles/%s" id)
     :edit-url          (format "/admin/story/%s" id)
     :revision-url      (format "/admin/story/%s/history/%s" id version)}))


(defn serialize-articles
  "A collection based form of serialize-articles so that it
  plays nicely with the threading macro"
  [articles]
  (map serialize articles))


(defn enable-revisions
  "Create a new version of the article which retains its publication
  information, but is free to stage changes"
  [article]
  (merge article {:written-on (java.time.LocalDateTime/now)
                  :state :revising}))


(defn mark-published
  "Change the articles state to `published` and apply some
  additional metadata"
  [{:keys [written-on publish-override version] :as article}]
  (merge article
         {:state :published
          :published-on (or publish-override written-on)
          :published-revision version
          :published-body (:body article)}))


(defn validate-can-publish
  "Quick and dirty sanity checks before allowing publishing"
  [{:keys [body title slug] :as article}]
  (if (some empty? [title body slug])
    (f/fail (ex-info "title, body, and slug fields are required for publishing" {}))
    article))


(defn mark-revoked
  "Revoke and article by dropping its published metadata and
  returning it to Draft mode."
  [article]
  (merge (dissoc article :published-on :published-revision)
         {:written-on (str (java.time.LocalDateTime/now))
          :state :draft}))


(defn publish-article
  "Validate an article can be published and mark
  it as published if so"
  [article]
  (f/ok-> article
          validate-can-publish
          mark-published))


(defn revoke-article
  "Revoke a published article"
  [{:keys [state] :as article}]
  (if (= state "draft")
    (f/fail (ex-info "Cannot revoke a draft, ya silly" {}))
    (mark-revoked article)))


(defn delete-article
  "Set the articles state to `deleted` and drops any
  publication meta-data if present"
  [article]
  (merge
    (mark-revoked article)
    {:written-on (str (java.time.LocalDateTime/now))
     :state      :deleted}))

