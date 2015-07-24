(ns emem.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string])
  (:use [clojure.java.io]
        [markdown.core]
        [hiccup.core])
  (:gen-class))

(def opts
  [
   ["-o" "--output FILE" "Output file"]
   ["-v" nil "Verbosity level; may be specified multiple times to increase value"
    ;; If no long-option is specified, an option :id must be given
    :id :verbosity
    :default 0
    ;; Use assoc-fn to create non-idempotent options
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Usage: emem [options] file[s]"
        ""
        "Options:"
        options-summary
        ]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn doc-title [file]
  (with-open [file (reader file)]
    (str (first (line-seq file)))))

(defn wrap-file [file]
  (html
   [:html
    [:head
     [:title (doc-title file)]
     [:meta {:http-equiv "Content-Type" :content "text/html;charset=utf-8"}]
     [:link {:rel "shortcut icon" :href "images/favicon.ico" :type "image/x-icon"}]
     [:link {:rel "stylesheet" :href "css/custom.css" :media "all"}]
     [:link {:rel "stylesheet" :href "http://cdnjs.cloudflare.com/ajax/libs/highlight.js/8.6/styles/zenburn.min.css"}]
     [:script {:src "http://cdnjs.cloudflare.com/ajax/libs/highlight.js/8.6/highlight.min.js"}]

     [:script "hljs.initHighlightingOnLoad();"]]
    [:body {:id "page-wrap"}
     (md-to-html-string (slurp file))]]))

(defn dump-file [options arguments]
  (let [[input] arguments
        output (or (:output options) "/dev/stdout")]
    (with-open [out (writer output)]
      (.write out (wrap-file input)))))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args opts)]
    (cond
      (:help options) (exit 0 (usage summary))
      (= (count arguments) 1) (dump-file options arguments)
      :else (exit 1 (error-msg errors)))))
