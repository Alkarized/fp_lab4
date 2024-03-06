(defproject tools "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring-cors/ring-cors "0.1.13"]
                 [ring/ring-codec "1.1.3"]
                 [metosin/ring-http-response "0.9.3"]
                 [ring/ring-json "0.5.0"]
                 [fogus/ring-edn "0.3.0"]
                 [codax/codax "1.4.0"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler tools.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
