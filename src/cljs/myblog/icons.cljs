(ns myblog.icons)


(defn gear-icon
  ([] (gear-icon 14 16))
  ([width height & [props]]
    [:svg (merge {:xmlns "http://www.w3.org/2000/svg"
                  :width width
                  :height height
                  :view-box "0 0 14 16"}
                 props)
     [:path {:fill-rule "evenodd"
             :d "M14 8.77v-1.6l-1.94-.64-.45-1.09.88-1.84-1.13-1.13-1.81.91-1.09-.45-.69-1.92h-1.6l-.63 1.94-1.11.45-1.84-.88-1.13 1.13.91 1.81-.45 1.09L0 7.23v1.59l1.94.64.45 1.09-.88 1.84 1.13 1.13 1.81-.91 1.09.45.69 1.92h1.59l.63-1.94 1.11-.45 1.84.88 1.13-1.13-.92-1.81.47-1.09L14 8.75v.02zM7 11c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3z"}]]))


(defn x-icon
  ([] (x-icon 12 16))
  ([width height & [props]]
    [:svg (merge {:xmlns "http://www.w3.org/2000/svg"
                  :width width
                  :height height
                  :view-box "0 0 12 16"}
                 props)
     [:path {:fill-rule "evenodd"
             :d "M7.48 8l3.75 3.75-1.48 1.48L6 9.48l-3.75 3.75-1.48-1.48L4.52 8 .77 4.25l1.48-1.48L6 6.52l3.75-3.75 1.48 1.48L7.48 8z"}]]))




(defn left-arrow
  ([] (left-arrow 10 16))
  ([width height & [props]]
   [:svg (merge {:xmlns "http://www.w3.org/2000/svg"
                 :width width
                 :height height
                 :view-box "0 0 10 16"}
                props)
    [:path {:fill-rule "evenodd"
            :d "M6 3L0 8l6 5v-3h4V6H6V3z"}]]))


(defn history
  ([] (history 10 16))
  ([width height & [props]]
   [:svg (merge {:xmlns "http://www.w3.org/2000/svg"
                 :width width
                 :height height
                 :view-box "0 0 12 16"}
                props)
    [:path {:fill-rule "evenodd"
            :d "M8 13H6V6h5v2H8v5zM7 1C4.81 1 2.87 2.02 1.59 3.59L0 2v4h4L2.5 4.5C3.55 3.17 5.17 2.3 7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-.34.03-.67.09-1H.08C.03 7.33 0 7.66 0 8c0 3.86 3.14 7 7 7s7-3.14 7-7-3.14-7-7-7z"}]]))


(defn trashcan
  ([] (trashcan 12 16))
  ([width height & [props]]
   [:svg (merge {:xmlns "http://www.w3.org/2000/svg"
                 :width width
                 :height height
                 :view-box "0 0 12 16"}
                props)
    [:path {:fill-rule "evenodd"
            :d "M11 2H9c0-.55-.45-1-1-1H5c-.55 0-1 .45-1 1H2c-.55 0-1 .45-1 1v1c0 .55.45 1 1 1v9c0 .55.45 1 1 1h7c.55 0 1-.45 1-1V5c.55 0 1-.45 1-1V3c0-.55-.45-1-1-1zm-1 12H3V5h1v8h1V5h1v8h1V5h1v8h1V5h1v9zm1-10H2V3h9v1z"}]]))



(defn settings
  ([] (settings 16 16))
  ([width height & [props]]
   [:svg (merge {:xmlns "http://www.w3.org/2000/svg"
                 :width width
                 :height height
                 :view-box "0 0 16 16"}
                props)
    [:path {:fill-rule "evenodd"
            :d "M4 7H3V2h1v5zm-1 7h1v-3H3v3zm5 0h1V8H8v6zm5 0h1v-2h-1v2zm1-12h-1v6h1V2zM9 2H8v2h1V2zM5 8H2c-.55 0-1 .45-1 1s.45 1 1 1h3c.55 0 1-.45 1-1s-.45-1-1-1zm5-3H7c-.55 0-1 .45-1 1s.45 1 1 1h3c.55 0 1-.45 1-1s-.45-1-1-1zm5 4h-3c-.55 0-1 .45-1 1s.45 1 1 1h3c.55 0 1-.45 1-1s-.45-1-1-1z"}]]))

(defn sign-out
  ([] (sign-out 16 16))
  ([width height & [props]]
   [:svg (merge {:xmlns "http://www.w3.org/2000/svg"
                 :width width
                 :height height
                 :view-box "0 0 16 16"}
                props)
    [:path {:fill-rule "evenodd"
            :d "M11.992 8.994V6.996H7.995v-2h3.997V2.999l3.998 2.998-3.998 2.998zm-1.998 2.998H5.996V2.998L2 1h7.995v2.998h1V1c0-.55-.45-.999-1-.999H.999A1.001 1.001 0 0 0 0 1v11.372c0 .39.22.73.55.91L5.996 16v-3.008h3.998c.55 0 1-.45 1-1V7.995h-1v3.997z"}]]))


(defn rocket
  ([] (rocket 16 16))
  ([width height & [props]]
   [:svg (merge {:xmlns "http://www.w3.org/2000/svg"
                 :width width
                 :height height
                 :view-box "0 0 16 16"}
                props)
    [:path {:fill-rule "evenodd"
            :d "M12.17 3.83c-.27-.27-.47-.55-.63-.88-.16-.31-.27-.66-.34-1.02-.58.33-1.16.7-1.73 1.13-.58.44-1.14.94-1.69 1.48-.7.7-1.33 1.81-1.78 2.45H3L0 10h3l2-2c-.34.77-1.02 2.98-1 3l1 1c.02.02 2.23-.64 3-1l-2 2v3l3-3v-3c.64-.45 1.75-1.09 2.45-1.78.55-.55 1.05-1.13 1.47-1.7.44-.58.81-1.16 1.14-1.72-.36-.08-.7-.19-1.03-.34a3.39 3.39 0 0 1-.86-.63zM16 0s-.09.38-.3 1.06c-.2.7-.55 1.58-1.06 2.66-.7-.08-1.27-.33-1.66-.72-.39-.39-.63-.94-.7-1.64C13.36.84 14.23.48 14.92.28 15.62.08 16 0 16 0z"}]]))


(defn lock
  ([] (lock 12 16))
  ([width height & [props]]
   [:svg (merge {:xmlns "http://www.w3.org/2000/svg"
                 :width width
                 :height height
                 :view-box "0 0 12 16"}
                props)
    [:path {:fill-rule "evenodd"
            :d "M4 13H3v-1h1v1zm8-6v7c0 .55-.45 1-1 1H1c-.55 0-1-.45-1-1V7c0-.55.45-1 1-1h1V4c0-2.2 1.8-4 4-4s4 1.8 4 4v2h1c.55 0 1 .45 1 1zM3.8 6h4.41V4c0-1.22-.98-2.2-2.2-2.2-1.22 0-2.2.98-2.2 2.2v2H3.8zM11 7H2v7h9V7zM4 8H3v1h1V8zm0 2H3v1h1v-1z"}]]))



(defn issue-opened
  ([] (issue-opened 14 16))
  ([width height & [props]]
   [:svg (merge {:xmlns "http://www.w3.org/2000/svg"
                 :width width
                 :height height
                 :view-box "0 0 14 16"}
                props)
    [:path {:fill-rule "evenodd"
            :d "M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}]]))


(defn eye
  ([] (eye 16 16))
  ([width height & [props]]
   [:svg (merge {:xmlns "http://www.w3.org/2000/svg"
                 :width width
                 :height height
                 :view-box "0 0 16 16"}
                props)
    [:path {:fill-rule "evenodd"
            :d "M8.06 2C3 2 0 8 0 8s3 6 8.06 6C13 14 16 8 16 8s-3-6-7.94-6zM8 12c-2.2 0-4-1.78-4-4 0-2.2 1.8-4 4-4 2.22 0 4 1.8 4 4 0 2.22-1.78 4-4 4zm2-4c0 1.11-.89 2-2 2-1.11 0-2-.89-2-2 0-1.11.89-2 2-2 1.11 0 2 .89 2 2z"}]]))
