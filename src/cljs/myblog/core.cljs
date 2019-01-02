(ns myblog.core
  (:require
            [re-frame.core :as rf]
            [reagent.core :as r]
            [myblog.db :as db]
            [myblog.subs :as subs]
            [myblog.events :as events]
            [myblog.util :as util]
            [myblog.views :as views]))



(defn ^:export run
  []
  (rf/dispatch-sync [:initialize])
  (rf/dispatch [:fetch-article (util/get-path-id)])
  (rf/dispatch [:fetch-revisions (util/get-path-id)])
  (r/render [views/editor-app] (js/document.getElementById "app")))

