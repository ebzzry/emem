(defproject emem "0.1.0-SNAPSHOT"
  :description "A trivial Markdown to HTML converter"
  :url "http://ebzzry.github.io"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [markdown-clj "0.9.67"]
                 [hiccup "1.0.5"]]
  :main ^:skip-aot emem.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
