(ns myblog.views
  (:require [cljsjs/moment]
            [cljsjs.showdown]
            [cljsjs.highlight]
            [cljsjs.highlight.langs.clojure]
            [cljsjs.highlight.langs.python]
            [cljsjs.highlight.langs.java]
            [myblog.icons :as icons]
            [myblog.util :as util]
            [myblog.pasty :as pasty]
            [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch]]))


(.setFlavor js/showdown "github")
(def markdown-converter (js/showdown.Converter.))


(defn metadata-form [form-data]
  [:div.metadata {:style {:padding "24px"}}
   [:h3.f3-light "Meta Data"]
   (map (fn [{:keys [error label name] :as props}]
          (let [update-fn #(dispatch [:update-form-field name (.-value (.-target %))])]
            [:dl.form-group {:class (if (empty? error) "" "errored")}
             [:dt [:label label]]
             [:dd.full-width [:input.form-control (merge props {:on-input update-fn})]]
             [:dd.error error]]))
        form-data)])


(defn metadata-modal [& {:keys [open on-cancel on-save children] :as props}]
  (when open
    [:div
     [:div.modal-overlay {:on-click on-cancel}]
     [:div.Box.Box--condensed.Box--overlay.d-flex.flex-column.anim-fade-in.fast.history-modal
      [:div.Box-header
       [:h3.Box-title "Edit Metadata"]]
      [:div.overflow-auto
       children]
      [:div.Box-footer
       [:div.pull-right
        [:button.btn.btn-spaced {:on-click #(dispatch [:open-metadata-modal false])} "Close"]
        [:button.btn.btn-primary.btn-spaced {:on-click #(dispatch [:save-metadata])} "Save!"]]]]]))



(defn history-modal [& {:keys [open revisions] :as props}]
  (when open
    [:div
     [:div.modal-overlay {:on-click #(dispatch [:open-revisions-modal false])}]
     [:div.Box.Box--condensed.Box--overlay.d-flex.flex-column.anim-fade-in.fast.history-modal
      [:div.Box-header
       [:h3.Box-title "Previous Revisions"]]
      [:div.overflow-auto
       [:div.Box-body {:style {:max-height "500px"}}
        (for [{:keys [revision-url written-on last-edited wordcount state]} revisions]
          [:div.Box-row.d-flex.flex-items-center
           [:div.flex-auto
            [:div
             [:strong (str "Revision - " last-edited)]
             [:span.Label.Label--outline.tag-margin {:style {:margin-left "10px"}} state]]
            [:div.text-small.text-gray-light
             (str "Edit date: " written-on " - (" wordcount ")")]]
           [:a {:href revision-url :target "_blank"} [:button.btn.btn-sm  "View"]]])
        ]]
      [:div.Box-footer
       [:button.btn.btn-block {:on-click #(dispatch [:open-revisions-modal false])} "Close"]]]]))


(defn publication-notice [{:keys [state published-revision] :as article}]
  (when (not= state "draft")
    [:div.flash {:class (if (= state "published") "flash-success" "flash-info")}
     (when (= state "published")
       [:div.btn.btn-sm.primary.flash-action
        {:on-click #(dispatch [:reopen-draft])}
        "Reopen for Editing"])
     (when (= state "revising")
       "You are currently making revisions. ")
     "This was published on " (str (js/moment published-revision))]))


(defn drawer-ctrl [close-handler]
  [:div.drawer-ctrl.drawer-header.default-flex.text-white.bg-gray-dark
   [:div.flex-fill [:h3.f3-light "Menu"]]
   [:div.close-button {:on-click close-handler} (icons/x-icon 16 16 {:style {:fill "white"}})]])


(defn drawer [{:keys [open state] :as params}]
  [:div.drawer {:class (when open "drawer-open")}
   [drawer-ctrl #(dispatch [:open-drawer false])]
   [:div
    [:div.drawer-row
     [icons/sign-out 18 18 {:class "icon"}] [:h3.f3-light "Sign Out"]]
    [:div.drawer-separator]
    ;[:div.drawer-row
    ; {:on-click #(dispatch [:open-metadata-modal true])}
    ; [icons/settings 18 18 {:class "icon"}] [:h3.f3-light "Inject Meta Data"]]
    [:div.drawer-row
     {:on-click #(dispatch [:open-revisions-modal true])}
     [icons/history 18 18 {:class "icon"}] [:h3.f3-light "Revisions"]]
    [:div.drawer-separator]
    (when (not= state "published")
      [:div.drawer-row
       {:on-click #(when (js/confirm "Are you sure you want to publish?")
                     (dispatch [:validate-can-publish]))}
       [icons/rocket 18 18 {:class "icon"}]
       [:h3.f3-light.text-green
        (if (= state "draft")
          "Publish"
          "Publish Changes")]])
    (when (not= "draft" state)
      [:div.drawer-row
       {:on-click #(when (js/confirm "Are you sure you revoke the published article?")
                     (dispatch [:revoke-article]))}
       [icons/issue-opened 18 18 {:class "icon"}] [:h3.f3-light.text-orange "Retract Published Article"]])
    [:div.drawer-separator]
    [:div.drawer-row
     {:on-click #(when (js/confirm "Are you sure you want to delete this article?")
                   (dispatch [:delete-article]))}
     [icons/trashcan 18 18 {:class "icon"}] [:h3.f3-light.text-red "Delete"]]
    ]])


(defn toolbar [& children]
  ^{:key "toolbar-container"}
  [:div.tool-bar
   children])


(defn back-button []
  [:div.pointer
   [:a.vertical-center {:href "/admin/"}
    (icons/left-arrow 20 20 {:style {:fill "#6a737d"}})]])


(defn title-input [{:keys [key title editable commit-changes] :as props}]
  (let [commit-change #(commit-changes (.-innerText (.-target %)))]
    [:div.title-area
     (when (not editable)
       [:span
        {:style {:position "relative" :top "3px"}}
        (icons/lock 18 18 {:style {:fill "#6a737d"}})])
     [:span.f3-light.title
      {:content-editable editable
       :on-blur commit-change
       :on-key-down #(when (#{"Enter" "Escape"} (.-key %))
                       (.preventDefault %)
                       (.blur (.-target %))
                       (commit-change %))}
      title]]))




(defn text-editor [& {:keys [body editable] :as props}]
  (let [editor-element (atom nil)]
    (reagent/create-class
      {:display-name "text-editor"
       :component-did-mount #(let [html (clojure.string/replace body "\n" "<br>")]
                               (reset! editor-element (.getElementById js/document "text-editor"))
                               (util/set-inner-text! "text-editor" body)
                               (.highlightBlock js/hljs (-> js/document (.querySelector "code"))))
       :reagent-render (fn []
                         ^{:key ":text-editor-body"}
                         [:div#text-editor.content-pane
                          {:style            {:flex "0 0 50%" :border-right "1px  solid #e1e4e8"}
                           :class (if @editable " " "hidden")
                           :on-paste pasty/handle-paste
                           :on-drop pasty/handle-drop
                           :on-input #(let [text (-> @editor-element .-innerText)]
                                        (dispatch [:update-body-text text]))
                           :content-editable @editable} ;(not= (:state article) "published")}
                          ])})))




(defn preview-pane [& {:keys [body visible] :as props}]
  ^{:key :preview-pane-key}
  (when-let [code-blocks (-> js/document (.querySelector "code"))]
    (.highlightBlock js/hljs code-blocks))

  [:div.content-pane {:style {:position "relative"
                              :flex "1 1 50%"}}
   [:div.preview-pane.markdown-body
     {:dangerouslySetInnerHTML {:__html (.makeHtml markdown-converter body)}}]])



(defn editor-body [& children]
  [:div.markdown-body {:style {:display "flex"
                               :height  "100%"
                               :width   "100%"}}
   children])


(defn menu-ctrl [on-click]
  [:div.vertical-center
    {:style {:cursor "pointer"}
     :on-click on-click}
    (icons/gear-icon 20 20 {:style {:fill "#6a737d"}})])


(defn saved-label [saved]
  [:div.Label.Label--outline
   {:style {:margin-right "10px"}
    :class (if saved "Label--outline-green" "Label--outline-gray")}
   (if saved "Saved" "Pending")])


(defn error-page [error]
  [:div "Move along. " (:message error)])


(defn editor-app
  "Our primary view entry point. Most subscriptions are wired up here"
  []
  (let [loading @(subscribe [:article/loading])
        error @(subscribe [:article/error])]
    (cond
      loading
        [:div "Just a sec..."]
      (and (not loading) (not (empty? error)))
        [error-page error]
      :else
        [:div
         [metadata-modal
            :open @(subscribe [:modals/metadata-open])
            :children [metadata-form @(subscribe [:metadata-form])]]
         [history-modal
            :open @(subscribe [:modals/revisions-open])
            :revisions @(subscribe [:revisions])]
         [publication-notice @(subscribe [:article])]
         [drawer {:open @(subscribe [:drawer-open])
                  :state @(subscribe [:article/state])}]
          [toolbar
           [back-button]
           [title-input {:title @(subscribe [:article/title])
                         :key @(subscribe [:article/title-key])
                         :editable @(subscribe [:editable])
                         :commit-changes #(dispatch [:commit-title-change %])}]
           [saved-label @(subscribe [:article/saved])]
           [menu-ctrl #(dispatch [:open-drawer true])]
           ]
         [editor-body
          [text-editor
              :body @(subscribe [:article/body])
              :editable (subscribe [:editable])]
          [preview-pane
              :body @(subscribe [:article/cleaned-body])
              :visible @(subscribe [:preview-pane-visible])]]
         ]
      )))



