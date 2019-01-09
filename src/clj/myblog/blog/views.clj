(ns myblog.blog.views
  (:require [hiccup.core :refer :all]
            [hiccup.page :refer [html5 include-css include-js]]
            [markdown.core :as md]
            [myblog.admin.util :as util]))

;; The blog side of things is entirely static. These functions exist to
;; build up an HTML doc, and then dump it to disk for direct serving


(def about-me!
  "
I'm a software developer and overall pretty cool guy. This blog is where I pretend to be an expert at things.

I currently work for [Amazon](https://www.amazon.com) and live in Seattle, WA.

I dig functional languages like Clojure and Haskell, but also love the 'second best for everything' language that is Python.

My big Open Source claim to fame is creating [Gooey](https://github.com/chriskiehl/Gooey).

Want me to build something for you? Hit me up! I lack loyalty to my current gig and am powered by a constant greedy desire for mo' money (and solving mo' problems).

Want Gooey releases to be churned out faster? [Sponsor its development](mailto:ckiehl@gmail.com?subject='I want to help support open source software with my money!')!

Just wanna chat? Drop me a line at [ckiehl@gmail.com](mailto:ckiehl@gmail.com). If you're a
recruiter, feel free to message me on [LinkedIn](https://www.linkedin.com/in/chris-kiehl-34426587/).
You can also follow me on [Github](https://github.com/chriskiehl), which is the extent of my Social Media.
")






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
      [:li [:a {:href "mailto:ckiehl@gmail.com"} "Contact"]]
      [:li [:a {:href "/rss.xml" :target "_blank"} "RSS Feed"]]]]])


(defn landing []
  [:div ])


(defn popular-preview [{:keys [title slug subtitle title-image title-image-srcset] :as article}]
  [:div.pop-prev
   [:a {:href (str "/article/" slug) :display "box"}
    [:div.thumb-container
     [:img.thumb-image {:src title-image
            :srcset title-image-srcset}]]]
   [:h4 title]
   [:div.sub-title
    [:h5 subtitle]]
   [:a {:href (str "/article/" slug)} [:span.light-link "Read >>"]]])



(defn most-recent [{:keys [title title-image title-image-srcset slug published-on] :as article}]
  (println (dissoc article :body :published-body))
  [:div.section
   [:div.most-recent
    [:div.article-preview
     [:h1 {:style "margin: 20px 0; font-size: 34px;"}
      [:span.section-header "Latest Post: "] title]
     [:div
      [:a {:href (str "/article/" slug)}
        [:img
          {:class "article-image"
           :src title-image
           :srcset title-image-srcset
           :alt (str "Title image for " title)
           }]]]
     [:div {:style "margin-top: 24px"}
      [:div (:description article)]
      [:a {:href (str "/article/" slug)} [:span.light-link "Read >>"]]]]]])



(defn footer []
  [:footer.footer
   (copyright-fixture)])


(defn base-page [& {:keys [title body]
                    :or  {title ""}
                    :as  params}]
  (html5
    [:head {:lang "en"}
     [:meta {:http-equiv "content-type"
             :content "text/html;charset=UTF-8"}]
     [:meta {:name "viewport"
             :content "width=device-width"}]
     (include-css "/css/styles.css")
     (include-css "/css/highlight.css")
     [:title (str title " - Blogomatano")]]
    [:body
     [:div.blog-container
      (sidebar)
      [:div.primary-content {:style "margin-top: 25px;"}
       body]
      ]
     (footer)
     (include-js "/js/external/google-analytics.js")
     (include-js "/js/external/highlight.js")
     "<script>hljs.initHighlightingOnLoad()</script>"]))


(defn most-popular [articles]
  [:section
   [:h3.section-header "Most Popular"]
   [:div.pop-section
    (map popular-preview articles)]])


(defn archive-link [{:keys [published-on title slug title-image title-image-srcset] :as article}]
  [:div {:style "margin: 10px 0"}
    [:div {:style "display: flex; align-items: center;  "}
     [:div.image-pill
      [:img.thumb-image {:src title-image
                         :srcset title-image-srcset}]]
     [:a.archive-link {:href (str "/article/" slug)}
      title [:span {:style "padding-left: 10px; font-size: 12px"}
             (format "(%s)" (util/calendar published-on))]]]])

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
   [:author "ckiehl@gmail.com"]])


(defn rss-feed [articles]
  [:rss {:version 2.0}
   [:channel
    [:title "chriskiehl.com"]
    [:link "https://chriskiehl.com"]
    [:description "The blog where I pretend to be good at stuff"]
    (map rss-item articles)]])


(defn home-page [articles]
  (base-page
    :title "Home"
    :body (if (empty? articles)
            [:div "uh oh. We didn't find anything. This is an error!"]
            [:div
             (most-recent (first articles))
             [:div.section.most-popular
              ;; todo: track page hits...
              (most-popular (filter #(#{"Cleaner-coding-through-partially-applied-functions"
                                        "parallelism-in-one-line"
                                        "packaging-gooey-with-pyinstaller"} (:slug %))
                                    articles))]
             [:section.section-last
              (all-articles articles)]])))


(defn about-page []
  (base-page
    :title "About"
    :body [:div
           [:img.about-photo {:src "https://s3.amazonaws.com/awsblogstore/main/images/self-portrait-2018.png"
                              :srcset "https://s3.amazonaws.com/awsblogstore/articles/52ac83c1/fa0e332c-self-portrait-2018-2960px.jpeg 2960w, https://s3.amazonaws.com/awsblogstore/articles/52ac83c1/fa0e332c-self-portrait-2018-2466px.jpeg 2466w, https://s3.amazonaws.com/awsblogstore/articles/52ac83c1/fa0e332c-self-portrait-2018-1972px.jpeg 1972w, https://s3.amazonaws.com/awsblogstore/articles/52ac83c1/fa0e332c-self-portrait-2018-1478px.jpeg 1478w, https://s3.amazonaws.com/awsblogstore/articles/52ac83c1/fa0e332c-self-portrait-2018-984px.jpeg 984w, https://s3.amazonaws.com/awsblogstore/articles/52ac83c1/fa0e332c-self-portrait-2018-490px.jpeg 490w"
                              :alt "Self Portrait - 2018"}]
           [:h1 "Howdy, Howdy! I'm Chris."]
           [:div.markdown-body (md/md-to-html-string about-me!)]]))


(defn article-page [{:keys [title published-body] :as article}]
  (base-page
    :title title
    :body [:div
           [:article (md/md-to-html-string published-body)]
           [:div#disqus_thread]
           (include-js "/js/external/disqus.js")]))


(defn error-404-page []
  (base-page
    :title "Uh, never heard of it, bub"
    :body [:div "Hey uh.. nothing is here. sweet 404 page pending.."]))



