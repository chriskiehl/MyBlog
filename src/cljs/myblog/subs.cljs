(ns myblog.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [myblog.markdown :as markdown]
            [myblog.parser :as parser]))


(reg-sub
  :article/error
  (fn [db _]
    (:article/error db)))


(reg-sub
  :article/loading
  (fn [db _]
    (:article/loading db)))


(reg-sub
  :article
  (fn [db _]
    (:article db)))

(reg-sub
  :article/title
  (fn [db _]
    (:active-title db)))


(reg-sub
  :article/title-key
  (fn [db _]
    (:active-title-key db)))


(reg-sub
  :article/saved
  (fn [db _]
    (:article/saved db)))


(reg-sub
  :drawer-open
  (fn [db _]
    (:drawer-open db)))


(reg-sub
  :article/body
  (fn [db _]
    (-> db :article :body)))

;

(reg-sub
  :article/cleaned-body
  (fn [db _]
    (let [body (-> db :article :body)]
      (-> (parser/remove-metadata-section body)
          markdown/replace-srcset-markdown))))

(reg-sub
  :article/state
  (fn [db _]
    (-> db :article :state)))


(reg-sub
  :editable
  (fn [db _]
    (not= "published" (-> db :article :state))))


(reg-sub
  :modals/metadata-open
  (fn [db _]
    (:metadata-modal-open db)))

(reg-sub
  :modals/revisions-open
  (fn [db _]
    (:history-model-open db)))

(reg-sub
  :revisions
  (fn [db _]
    (:revisions db)))

(reg-sub
  :metadata-form
  (fn [db _]
    (let [form (:metadata-form db)
          {:keys [title-image title-image-srcset slug publish-override]} form]
      [title-image title-image-srcset slug publish-override])))

(reg-sub
  :preview-pane-visible
  (fn [db _]
    (:preview-pane-visible db)))
