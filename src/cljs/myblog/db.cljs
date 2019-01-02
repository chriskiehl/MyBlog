(ns myblog.db)


(def default-db
  {
   :article/loading true
   :article/error {}
   :article/saved true
   :drawer-open false
   :saving-draft false
   :mounted-text? false
   :fetching-article true
   :history-model-open false
   :metadata-modal-open false
   :article-retrieve-error {}
   :not-found false
   :saved true
   :preview-pane-visible true
   :active-title ""
   ;; i can't bloody figure out how
   ;; to get the fudging thing to render
   ;; without manually modifying the key
   :active-title-key ""
   :article {}
   :revisions []
   :metadata-form {
       :title-image {
           :placeholder  "s3://some-sweet-pic.jpg"
           :type         "text"
           :value        ""
           :error        ""
           :style {:width "100%"}
           :name :title-image
           :label        "Title Image"}
       :title-image-srcset {
           :placeholder  "s3://image1.jpg 200px, s3://image1.jpg 100px"
           :type         "text"
           :value        ""
           :error        ""
           :style {:width "100%"}
           :name :title-image-srcset
           :label        "Title Image Source Set"}
       :slug {
           :placeholder  "your-title-here"
           :type         "text"
           :value        ""
           :error        ""
           :style {:width "100%"}
           :name :slug
           :label        "URL Slug"}
       :publish-override {
           :placeholder  (.toISOString (js/Date.))
           :type         "text"
           :value        ""
           :style {:width "100%"}
           :name :publish-override
           :error        ""
           :label        "Publication Date Override"}
       :description {
           :placeholder  (.toISOString (js/Date.))
           :type         "text"
           :value        ""
           :style {:width "100%"}
           :name :publish-override
           :error        ""
           :label        "Description"}}
   })
