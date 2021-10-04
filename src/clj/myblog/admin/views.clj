(ns myblog.admin.views
  (:require [hiccup.core :refer :all]
            [hiccup.page :refer [html5 include-css]]
            [markdown.core :as md]))


(defn base-page [{:keys [title body] :as params}]
  (html5
    [:head {:lang "en"}
     [:meta {:http-equiv "content-type"
             :content "text/html;charset=UTF-8"}]
     [:meta {:name "viewport"
             :content "width=device-width"}]
     (include-css "/css/primer.css")
     (include-css "/css/admin.css")
     [:title title]]
    [:body body]))


(defn login-form [{:keys [error] :as props}]
  [:form.text-center
   {:action "/login" :method "POST"
    :style (str "border: 1px solid #cacaca;"
                "padding: 30px;"
                "width: 400px;"
                "margin: 60px auto auto auto;"
                "display: flex;"
                "flex-direction: column;"
                "align-items: center;")}
   [:img {:src "https://s3.amazonaws.com/awsblogstore/main/images/admin-image.jpg"}]
   (when error
     [:div.flash.flash-error
      "Unknown username or password"])
   [:div.form-group
    [:input.form-control {:style "width: 100%"
                          :type "text"
                          :name "username"
                          :placeholder "username"}]]
   [:div.form-group {:style "margin-top: 10px"}
    [:input.form-control {:style "width: 100%"
                          :type "password"
                          :name "password"
                          :placeholder "Password"}]]
   [:button.btn.btn-orange
    {:type "submit"
     :style "margin-top: 14px"}
    "Access this blog!"]])


(defn empty-state []
  [:div.blankslate
   [:h3 "No articles created yet"]
   [:p "You swine! You lazy pig bastard! The internet is too good for your kind!"]
   [:form {:action "/admin/new" :method "post"}
    [:button.btn "Get Started!"]]])



(defn dashboard-article [{:keys [title edit-url creation-date-str last-edited wordcount state] :as article}]
  [:div.Box-row.d-flex.flex-items-center
   [:div.flex-auto
    [:div [:strong title ]
     (if (not= state "draft")
       [:span.Label.Label--outline-green.tag-margin "Published"]
       [:span.Label.Label--outline.tag-margin "Draft"])]
    [:div.text-small.text-gray-light
     (format "Created %s  -  Last edited %s  -  (%s)" creation-date-str last-edited wordcount)]]
   [:a {:href edit-url} [:button.btn.btn-sm  "Edit"]]])


(defn dashboard-articles [articles & {:keys [condensed]
                                      :or {condensed false}}]
  [:div.Box {:style "margin-bottom: 60px"
             :class (and condensed "Box--condensed")}
   (map dashboard-article articles)])


(defn config-controls []
  [:div.Box {:style "margin-bottom: 60px" :class "Box--condensed"}

    [:div.Box-row.d-flex.flex-items-center
     [:div.flex-auto
      [:div [:strong "About Blurb"]
       [:span.Label.Label--outline.tag-margin "Cool Summary, Bro"]]
      [:div.text-small.text-gray-light
       "View or manage the about blurb on the pages"]]
     [:a {:href "/admin/about"} [:button.btn.btn-sm  "Edit"]]]

   [:div.Box-row.d-flex.flex-items-center
    [:div.flex-auto
     [:div [:strong "Patrons"]
      [:span.Label.Label--outline.tag-margin "The real heros"]]
     [:div.text-small.text-gray-light
      "View or manage the current Patreon supporters"]]
    [:a {:href "/admin/patrons"} [:button.btn.btn-sm  "Edit"]]]])


(defn header []
  [:div.admin-header
   [:img.dats-me {:src "https://awsblogstore.s3.amazonaws.com/main/images/circle_avatar.png"}]
   [:div {:style "flex: 1 1 auto; padding-left: 16px;"} [:h3.f3-light {:style "margin: 0"} "Admin"]]
   [:form {:action "/logout" :method "POST"}
    [:button.btn {:type "submit"} "Logout"]]])

(defn dashboard [articles]
  [:div {:style "width: 100%"}
   (header)
   [:div.dashboard-content
    [:div
     [:div.article-pagehead
      [:h2.f2-light "Config"]]
     (config-controls)]]
   [:div.dashboard-content
    (when (not (empty? articles))
      (let [recents (take 2 (reverse (sort-by :written-on articles)))]
        [:div
         [:div.article-pagehead
          [:h2.f2-light "Recent Activity"]]
         (dashboard-articles recents :condensed true)]))
    [:div
     [:div.article-pagehead
      [:h2.f2-light {:style "flex: 1 1 auto"} "Articles"]
      [:div {:style "flex: 0 0 auto"}
       [:form {:action "/admin/new" :method "post"}
        [:button.btn.btn-sm.btn-primary {:type "submit"} "New Article"]]]]

      (if (empty? articles)
        (empty-state)
        (dashboard-articles articles))]]])


(defn raw-history [{:keys [body written-on] :as article}]
  (base-page {:title (str "revision - " written-on)
              :body  [:pre body]}))


(defn -about-editor [blurb-text]
  [:div
   (header)
   [:div.dashboard-content
     [:form.form-group
      {:action "/admin/about" :method "post"}
      [:div.form-group-header
       [:label {:for "example-textarea"} "About Blurb"]]
      [:div.form-group-body
       [:textarea#example-textarea.form-control
        (:body blurb-text)]]
      [:button.btn.btn-primary
       {:type "submit" :style "float: right; margin-top: 20px;"}
       "Save"]]]])


(defn login-page [& [props]]
  (base-page {:title "chriskiehl.com - No sneaky business, now..."
              :body (login-form props)}))


(defn admin-dashboard [articles]
  (base-page {:title "chriskiehl.com - Dashboard"
              :body (dashboard articles)}))


(defn about-editor [blurb-text]
  (base-page {:title "chriskiehl.com - Edit about section"
              :body (-about-editor blurb-text)}))
