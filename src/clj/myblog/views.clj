(ns myblog.views
  (:require [hiccup.core :refer :all]
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.element :refer [javascript-tag]]
            [markdown.core :as md]
            [myblog.markdown :refer [srcset]]
            [hiccup.core :as hiccup]))


(defn copyright-fixture []
  (let [year (.getYear (java.time.LocalDateTime/now))]
    [:div {:style "float: right"}
     [:small (format "Copyright © 2014-%s" year)]]))


(defn sidebar []
   [:header.sidebar
    [:div.icon
     [:a {:href "/"}
      [:img.dats-me {:src "https://awsblogstore.s3.amazonaws.com/main/images/circle-avatar-tall.png"}]]]
    [:nav
     [:ul.list-unstyled.primary-nav
      [:li [:a {:href "/about"} "About"]]
      [:li [:a {:href "https://github.com/chriskiehl" :target "_blank"} "Projects"]]
      [:li [:a {:href "mailto:me@chriskiehl.com"} "Contact"]]
      [:li [:a {:href "https://www.manning.com/books/data-oriented-programming-in-java"} "Book"]]
      [:li [:b [:a {:href "/help-plz"} "Halp!"]]]
      [:li [:a {:href "/rss.xml" :target "_blank"} "RSS"]]]]])


(defn popular-preview [{:keys [title slug title-images] :as article}]
  [:div.pop-prev
   [:a {:href (str "/article/" slug) :display "box"}
    [:div.thumb-container
     [:img.thumb-image {:src (-> title-images :images first :url)
            :srcset (srcset (:images title-images))}]]]
   [:h4 title]
   [:a {:href (str "/article/" slug)} [:span.light-link "Read >>"]]])



(defn most-recent [{:keys [title title-images slug] :as article}]
  [:div.section
   [:div.most-recent
    [:div.article-preview
     [:h1 {:style "margin: 20px 0; font-size: 34px;"}
      [:span.section-header "Latest Post: "] title]
     [:div
      [:a {:href (str "/article/" slug)}
        [:img
          {:class "article-image"
           :src (:url (first (:images title-images)))
           ;:srcset (srcset title-images)
           :alt (str "Title image for " title)
           }]]]
     [:div {:style "margin-top: 24px"}
      [:div (:description article)]
      [:a {:href (str "/article/" slug)} [:span.light-link "Read >>"]]]]]])



(defn footer []
  [:footer.footer
   (copyright-fixture)
   [:div {:style "clear: both"}]
   [:div {:style "float: right"}
    [:small (format "Made by a human being. Not GPT")]]])


(defn base-page [& {:keys [title body meta]
                    :or  {title ""}
                    :as  params}]
  (html5
    [:head {:lang "en"}
     [:meta {:http-equiv "content-type"
             :content "text/html;charset=UTF-8"}]
     [:meta {:name "viewport"
             :content "width=device-width"}]
     [:meta {:name "description"
             :content (or meta "A pretty cool website")}]
     [:script#MathJax-script {:async "true" :src "/js/external/highlight.js"}]
     (javascript-tag "window.MathJax = {
        tex: {inlineMath: [['$', '$'], ['\\\\(', '\\\\)']]}};")
     (include-css "/css/styles.css")
     (include-css "/css/highlight.css")
     [:title (str title " - Blogomatano")]]
    [:body
     [:div.blog-container
      (sidebar)
      [:div.primary-content {:style "margin-top: 25px;"}
       body]]
     (footer)
     [:script {:defer "true"
               :src "https://static.cloudflareinsights.com/beacon.min.js"
               :data-cf-beacon "{\"token\": \"d3e0e137186d4250959fda9d93cfaeff\"}"}]
     (include-js "/js/external/highlight.js")
     "<script>hljs.initHighlightingOnLoad()</script>"
     ]))


(defn most-popular [articles]
  [:section
   [:h3.section-header "Most Popular"]
   [:div.pop-section
    (map popular-preview articles)]])


(defn archive-link [{:keys [published-on title slug title-images] :as article}]
  [:div {:style "margin: 10px 0"}
    [:div {:class "previous-article"}
     [:div.image-pill
      [:img.thumb-image {:src (-> title-images :images last :url)
                         :srcset (srcset (:images title-images))}]]
     [:a.archive-link {:href (str "/article/" slug)}
      title [:span {:style "padding-left: 10px; font-size: 12px"}
             published-on]]]])

(defn all-articles [articles]
  [:div
   [:h3.section-header "All Posts"]
   (map archive-link articles)])


(defn rss-item [{:keys [title description slug] :as article}]
  [:item
   [:title title]
   [:link (str "https://chriskiehl.com/article/" slug)]
   [:description description]
   [:pubDate (:published-on article)]
   [:author "me@chriskiehl.com"]])


(defn rss-feed [articles]
  (hiccup/html
    [:rss {:version 2.0}
     [:channel
      [:title "chriskiehl.com"]
      [:link "https://chriskiehl.com"]
      [:description "The blog where I pretend to be good at stuff"]
      (map rss-item articles)]]))


(defn home-page [articles popular]
  (base-page
    :title "Home"
    :body (if (empty? articles)
            [:div "uh oh. We didn't find anything. This is an error!"]
            [:div
             (most-recent (first articles))
             [:section.section-last
              (all-articles articles)]
             ])))


(defn about-page [about-me!]
  (base-page
    :title "About"
    :body [:div
           [:img.about-photo {:src "https://s3.amazonaws.com/awsblogstore/main/images/self-portrait-2018.png"
                              :srcset "https://s3.amazonaws.com/awsblogstore/articles/52ac83c1/fa0e332c-self-portrait-2018-2960px.jpeg 2960w, https://s3.amazonaws.com/awsblogstore/articles/52ac83c1/fa0e332c-self-portrait-2018-2466px.jpeg 2466w, https://s3.amazonaws.com/awsblogstore/articles/52ac83c1/fa0e332c-self-portrait-2018-1972px.jpeg 1972w, https://s3.amazonaws.com/awsblogstore/articles/52ac83c1/fa0e332c-self-portrait-2018-1478px.jpeg 1478w, https://s3.amazonaws.com/awsblogstore/articles/52ac83c1/fa0e332c-self-portrait-2018-984px.jpeg 984w, https://s3.amazonaws.com/awsblogstore/articles/52ac83c1/fa0e332c-self-portrait-2018-490px.jpeg 490w"
                              :alt "Self Portrait - 2018"}]
           [:h1 "Howdy, Howdy! I'm Chris."]
           [:div.markdown-body (md/md-to-html-string about-me!)]]))


(defn article-page [{:keys [title description static-content] :as article}]
  (base-page
    :title title
    :meta description
    :body [:div
           [:article {:id "article"} static-content]]))



