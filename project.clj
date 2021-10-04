(defproject myblog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.439"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [amazonica "0.3.138"]
                 [environ "1.1.0"]
                 [failjure "1.3.0"]
                 [ring/ring-json "0.4.0"]
                 [org.imgscalr/imgscalr-lib "4.2"]
                 [com.twelvemonkeys.imageio/imageio "3.7.0"]
                 [com.twelvemonkeys.imageio/imageio-jpeg "3.7.0"]
                 [reagent "0.7.0"]
                 [re-frame "0.10.6"]
                 [cljsjs/moment "2.22.2-2"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.clojure/data.json "0.2.6"]
                 [cljs-ajax "0.5.1"]
                 [ring/ring-json "0.4.0"]
                 [com.amazonaws/aws-java-sdk "1.11.464"]
                 [markdown-clj "1.0.5"]
                 [org.ocpsoft.prettytime/prettytime "3.2.7.Final"]
                 [hiccup "1.0.5"]
                 [org.clojure/core.async "0.4.490"]
                 [compojure "1.6.1"]
                 ;[rum "0.11.2"]
                 [figwheel-sidecar "0.5.18"]
                 [buddy "2.0.0"]
                 [buddy/buddy-auth "2.1.0"]
                 [com.fasterxml.jackson.core/jackson-databind "2.9.7"]
                 [cljsjs/showdown "1.8.6-0"]
                 [cljsjs/highlight "9.12.0-2"]
                 [cljsjs/showdown "1.8.6-0"]]
  :ring {:handler myblog.core/app}
  :plugins [[lein-environ "1.1.0"]
            [lein-exec "0.3.7"]
            [lein-ring "0.12.5"]
            [lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.18"]]
  :source-paths ["src/clj" "script"]
  :clean-targets [:target-path "out"]
  :cljsbuild {
              :builds {
                       :main {:figwheel  {:on-jsload "myblog.core/run"}
                              :source-paths ["src/cljs"]
                              :compiler {:main "myblog.core"
                                         :source-map           true
                                         :source-map-timestamp true
                                         :asset-path "/js/out"
                                         :output-dir "resources/public/js/out"
                                         :output-to "resources/public/js/main.js"
                                         :optimizations :none}}
                       :prod {:source-paths ["src/cljs"]
                              :compiler {:main "myblog.core"
                                         :output-to "resources/public/js/main.js"
                                         :optimizations :advanced
                                         :pretty-print false}}}}
  :main myblog.core
  :uberjar-exclusions [#"repl.clj"]
  :figwheel
  {:http-server-root "public"
   :server-port 3000
   :css-dirs ["resources/public/css"]}

  :profiles {:uberjar {:aot :all}
             :dev [:project/dev :profiles/dev]
             :prod [:project/prod :profiles/prod]
             :profiles/dev {}
             :project/dev {:env {:aws-secret-key "super"
                                 :aws-access-key "secret"
                                 :aws-endpoint "us-west-1"
                                 :table-name "dev-articles"
                                 :username "admin"
                                 :password "pass"}}
             :project/prod {:env {:aws-secret-key nil
                                  :aws-access-key nil
                                  :aws-endpoint "us-west-1"
                                  :table-name "articles"}}})
