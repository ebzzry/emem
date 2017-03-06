(defproject emem "0.2.31-SNAPSHOT"
  :description "A trivial Markdown to HTML converter"
  :url "http://ebzzry.github.io/emem"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/data.codec "0.1.0"]
                 [markdown-clj "0.9.89"]
                 [hiccup "1.0.5"]
                 [cpath-clj "0.1.2"]]
  :main ^:skip-aot emem.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
