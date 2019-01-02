(ns myblog.events
  (:require [ajax.core :refer [GET PUT DELETE]]
            [myblog.forms :as forms]
            [myblog.db :as db]
            [myblog.util :as util]
            [myblog.parser :as parser]
            [re-frame.core :as rf]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.string :refer [blank?]]))

(def meta-section
  (clojure.string/trim "
[metadata]
title-image:
slug:
description:
publish-date:
[end-metadata]"
  ))


(rf/reg-event-db
  :initialize
  (fn [_ _]
    db/default-db))


(rf/reg-event-db
  :open-drawer
  (fn [db [_ open?]]
    (assoc db :drawer-open open?)))


(rf/reg-event-db
  :open-metadata-modal
  (fn [db [_ open?]]
    (-> db
        (assoc :metadata-modal-open open?)
        (assoc :drawer-open false)
        (forms/clear-form-errors)
        (forms/copy-latest-values))))


(rf/reg-event-db
  :fetch-article
  (fn [db [_ id]]
    (GET (str "/api/articles/" id)
         {:handler #(rf/dispatch [:process-article %])
          :error-handler #(rf/dispatch [:article-fetch-failed %])})
    (assoc db :article/loading true)))


(rf/reg-event-db
  :process-article
  (fn [db [_ response]]
    (let [article (keywordize-keys response)]
      (merge db {:article/loading false
                 :article/saved true
                 :article article
                 :active-title (-> article :title)}))))


(rf/reg-event-db
  :article-fetch-failed
  (fn [db [_ response]]
    (merge db {:article/loading false
               :article/error (-> response keywordize-keys :response)})))


(rf/reg-event-db
  :fetch-revisions
  (fn [db [_ id]]
    (GET (str "/api/articles/" id "/history")
         {:handler #(rf/dispatch [:process-revisions %])
          :error-handler #(rf/dispatch [:revision-fetch-failed %])})
    (assoc db :article/loading true)))


(rf/reg-event-db
  :process-revisions
  (fn [db [_ response]]
    (let [revisions (map keywordize-keys response)]
      (assoc db :revisions revisions))))


(rf/reg-event-db
  :revision-fetch-failed
  (fn [db [_ response]]
    (js/console.error "failed while trying to fetch revisions")
    (js/console.error (clj->js response))
    (js/alert "failed while trying to fetch revisions. See log for details")
    (merge db {:revisions/loading false
               :revisions/error (-> response keywordize-keys :response)})))


(defn can-publish [{:keys [title-image slug description] :as article}]
  (let [not-blank (complement blank?)]
    (and (not-blank title-image) (not-blank slug) (not-blank description))))


(rf/reg-event-fx
  :validate-can-publish
  (fn [{:keys [db] :as cofx} _]
    (if (can-publish (:article db))
      {:db db
       :dispatch [:publish-article]}
      (do (js/alert "Title image, description, and slug meta-data must be present in order to publish!")  ; lazy..
          {:db db
           :dispatch [:open-drawer false]}))))


(rf/reg-event-fx
  :publish-article
  (fn [{:keys [db] :as cofx} _]
    (let [article (:article db)]
      (PUT (str "/api/articles/" (-> article :id) "/publish")
           :handler #(rf/dispatch [:process-article %])
           :error-handler #(rf/dispatch [:article-save-failed %]))
      {:db db
       :dispatch [:open-drawer false]})))


(rf/reg-event-fx
  :revoke-article
  (fn [{:keys [db] :as cofx} _]
    (let [article (:article db)]
      (DELETE (str "/api/articles/" (-> article :id) "/publish")
           :handler #(rf/dispatch [:process-article %])
           :error-handler #(rf/dispatch [:revoke-article-failed %]))
      {:db db
       :dispatch [:open-drawer false]})))


(rf/reg-event-fx
  :reopen-draft
  (fn [{:keys [db] :as cofx} _]
    (let [article (:article db)]
      (PUT (str "/api/articles/" (-> article :id) "/unlock")
           :handler #(rf/dispatch [:process-article %])
           :error-handler #(rf/dispatch [:article-save-failed %]))
      {:db db})))


(rf/reg-event-fx
  :commit-title-change
  (fn [{:keys [db] :as cofx} [_ updated]]
    (let [article (:article db)
          original (-> db :article :title)
          invalid-title #(empty? %)]
      (if (invalid-title updated)
        {:db (-> db
                 (assoc :active-title original)
                 (update :active-title-key inc))}
        {:db (-> db
                 (assoc-in [:article :title] updated)
                 (assoc :active-title updated)
                 (assoc :article/saved false))
         :dispatch [:save-article article]}))))


(rf/reg-event-db
  :merge-metadata
  (fn [db _]
    (let [{:keys [slug publish-override title-image title-image-srcset]} (:metadata-form db)]
      (-> db
          (assoc-in [:article :slug] (:value slug))
          (assoc-in [:article :title-image] (:value title-image))
          (assoc-in [:article :title-image-srcset] (:value title-image-srcset))
          (assoc-in [:article :publish-override] (:value publish-override))))))




(rf/reg-event-fx
  :save-metadata
  (fn [{:keys [db] :as cofx} _]
    (if-let [errors (forms/valid-metadata? (:metadata-form db))]
      {:db (forms/add-errors (forms/clear-form-errors db) errors)}
      ;; otherwise: save it!
      {:db (:db cofx)
       :dispatch-n [[:open-metadata-modal false]
                    [:save-article (forms/merge-metadata db)]]})))


(rf/reg-event-db
  :save-article
  (fn [db [_ article]]
    (PUT (str "/api/articles/" (-> article :id))
         {:params article
          :format :json
          :handler #(rf/dispatch [:process-article %])
          :error-handler #(rf/dispatch [:article-save-failed %])})
    (assoc db :article/saving true)))


(rf/reg-event-fx
  :save-current
  (fn [{:keys [db] :as cofx} _]
    (let [article (:article db)
          body (:body article)
          metadata (-> (parser/parse-metadata body)
                        forms/maybe-canonicalize-date)
          cleaned-meta (util/drop-blank-vals metadata)
          article' (merge article cleaned-meta)]
      {:db db
       :dispatch [:save-article article']})))


(rf/reg-event-db
  :article-save-failed
  (fn [db [_ response]]
    (js/console.error (clj->js response))
    (js/alert response)
    db))


(rf/reg-event-db
  :update-form-field
  (fn [db [_ name value]]
    (assoc-in db [:metadata-form name :value] value)))


(rf/reg-event-db
  :open-revisions-modal
  (fn [db [_ open?]]
    (-> db
        (assoc :history-model-open open?)
        (assoc :drawer-open false))))


; Saves the current article after the user hasn't produced
; any input for 700ms.
(def throttled-save
  (util/debouncer #(rf/dispatch [:save-current]) 700))


(rf/reg-event-db
  :update-body-text
  (fn [db [_ text]]
    (throttled-save)
    (-> db
        (assoc :article/saved false)
        (assoc-in [:article :body] text))))



(rf/reg-event-fx
  :delete-article
  (fn [{:keys [db] :as cofx} _]
    (let [article (:article db)]
      (DELETE (str "/api/articles/" (-> article :id))
           :handler #(set! (.-location js/window) "/admin/")
           :error-handler #(rf/dispatch [:article-save-failed %]))
      {:db db
       :dispatch [:open-drawer false]})))


(rf/reg-event-fx
  :paste-plain-text
  (fn [cofx [_ transfer-object]]
    ;(let [paste-event (or (get-text transfer-object) (get-files transfer-object))]
      {:db (:db cofx)}))


(rf/reg-event-db
  :sign-url
  (fn [db _]
    (let [article (:article db)]
      (GET (str "/api/sign-url/")
              :handler #(set! (.-location js/window) "/admin/")
              :error-handler #(do (js/alert "unable to save image. See logs for details")
                                  (js/console.error (clj->js %))))
      {:db db
       :dispatch [:open-drawer false]})))



(rf/reg-event-db
  :toggle-preview-pane
  (fn [db _]
    (let [visible (:preview-pane-visible db)]
      (assoc db :preview-pane-visible (not visible)))))