(defproject myblog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :repositories [["central" "https://maven-central.storage-download.googleapis.com/maven2/"]
                 ["clojars" "https://repo.clojars.org/"]]

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.439"]

                 [ring/ring-core "1.9.6"]
                 [ring/ring-jetty-adapter "1.9.6"]
                 [ring/ring-json "0.5.1"]

                 [amazonica "0.3.138"
                  :exclusions [com.amazonaws/aws-java-sdk
                               com.amazonaws/amazon-kinesis-client
                               com.amazonaws/dynamodb-streams-kinesis-adapter]]

                 ;; AWS SDK v1 modules used instead of the giant aws-java-sdk bundle
                 [com.amazonaws/aws-java-sdk-core "1.11.464"]
                 [com.amazonaws/aws-java-sdk-s3 "1.11.464"]
                 [com.amazonaws/aws-java-sdk-dynamodb "1.11.464"]
                 [com.amazonaws/aws-java-sdk-logs "1.11.464"]

                 [environ "1.1.0"]
                 [failjure "1.3.0"]

                 [org.imgscalr/imgscalr-lib "4.2"]
                 [com.twelvemonkeys.imageio/imageio-core "3.7.0"]
                 [com.twelvemonkeys.imageio/imageio-jpeg "3.7.0"]

                 [reagent "0.7.0"]
                 [re-frame "0.10.6"]
                 [cljsjs/moment "2.22.2-2"]

                 [org.clojure/tools.logging "0.4.1"]
                 [org.clojure/data.json "0.2.6"]
                 [cljs-ajax "0.5.1"]

                 [markdown-clj "1.10.6"]
                 [org.ocpsoft.prettytime/prettytime "3.2.7.Final"]
                 [hiccup "1.0.5"]
                 [org.clojure/core.async "0.4.490"]
                 [compojure "1.6.1"]

                 [buddy/buddy-auth "2.1.0"]

                 [com.fasterxml.jackson.core/jackson-databind "2.13.5"]

                 [cljsjs/showdown "1.8.6-0"]
                 [cljsjs/highlight "9.12.0-2"]

                 [com.cognitect.aws/api "0.8.596"]
                 [com.cognitect.aws/endpoints "1.1.12.307"]
                 [com.cognitect.aws/logs "822.2.1145.0"]
                 [com.cognitect.aws/s3 "822.2.1145.0"]]

  :ring {:handler myblog.core/app}

  :plugins [[lein-environ "1.1.0"]]

  :source-paths ["src/clj" "script"]
  :clean-targets [:target-path "out"]

  :cljsbuild
  {:builds
   {:main {:figwheel {:on-jsload "myblog.core/run"}
           :source-paths ["src/cljs"]
           :compiler {:main "myblog.core"
                      :source-map true
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
                                 :stage :dev
                                 :vault-url "some/file/path"
                                 :s3-host ""}}

             :project/prod {:env {:aws-secret-key nil
                                  :aws-access-key nil
                                  :aws-endpoint "us-west-1"
                                  :table-name "articles"
                                  :stage :prod
                                  :vault-url "some/file/path"
                                  :s3-host ""}}})
