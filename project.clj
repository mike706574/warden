(defproject org.clojars.mike706574/warden "0.0.1-SNAPSHOT"
  :description "A REST API for issuing JWT tokens."
  :url "https://github.com/mike706574/warden-webapp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [org.clojure/spec.alpha "0.1.123"]
                 [com.stuartsierra/component "0.3.2"]

                 ;; Utility
                 [org.clojars.mike706574/boomerang "0.1.0-SNAPSHOT"]
                 [environ "1.1.0"]
                 [clj-time "0.13.0"]

                 ;; Logging
                 [com.taoensso/timbre "4.10.0"]

                 ;; Web
                 [aleph "0.4.3"]
                 [ring/ring-anti-forgery "1.1.0"]
                 [ring-cors "0.1.11"]
                 [ring/ring-defaults "0.3.0"]
                 [compojure "1.6.0"]

                 ;; Security
                 [buddy/buddy-hashers "1.2.0"]
                 [buddy/buddy-sign "1.5.0"]]
  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj"]
  :plugins [[cider/cider-nrepl "0.15.0-SNAPSHOT"]
            [org.clojure/tools.nrepl "0.2.12"]
            [lein-cloverage "1.0.9"]]
  :profiles {:dev {:source-paths ["dev"]
                   :target-path "target/dev"
                   :dependencies [[org.clojure/test.check "0.9.0"]
                                  [org.clojure/tools.namespace "0.2.11"]]}
             :production {:aot :all
                          :main warden.main
                          :uberjar-name "warden-webapp.jar"}})
