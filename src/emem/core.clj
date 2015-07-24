(ns emem.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string])
  (:use [clojure.java.io]
        [markdown.core]
        [hiccup.core])
  (:gen-class))

(def cli-opts
  [
   ["-o" "--output HTML_FILE" "Output file; defaults to stdout"]
   ["-v" nil "Verbosity level; may be specified multiple times to increase value"
    :id :verb
    :default 0
    ;; Use assoc-fn to create non-idempotent options
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help"]])

(defn usage [opts-summary]
  (->> ["Usage: emem [OPTION]... MARKDOWN_FILE..."
        ""
        "Options:"
        opts-summary
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

;; If the -v level is >= current code level, text is printed
(defn msg [text level req]
  (when (>= level req)
    (println text)))

(defn html-wrapper [opts args text]
  (let [[lead & _] args]
    (html
     [:html
      [:head
       [:title (doc-title lead)]
       [:meta {:http-equiv "Content-Type" :content "text/html;charset=utf-8"}]
       [:link {:rel "shortcut icon" :href "images/favicon.ico" :type "image/x-icon"}]
       [:link {:rel "stylesheet" :href "css/custom.css" :media "all"}]
       [:link {:rel "stylesheet" :href "http://cdnjs.cloudflare.com/ajax/libs/highlight.js/8.6/styles/zenburn.min.css"}]
       [:script {:src "http://cdnjs.cloudflare.com/ajax/libs/highlight.js/8.6/highlight.min.js"}]
       [:script "hljs.initHighlightingOnLoad();"]]
      [:body {:id "page-wrap"}
       text]])))

(defn wrap [opts args]
  (msg "Loading input files ..." (:verb opts) 1)
  (html-wrapper opts args
                (apply str (map #(md-to-html-string (slurp %)) args))))

(defn dump [opts args]
  (msg "Writing output files ..." (:verb opts) 1)
  (let [output (or (:output opts) "/dev/stdout")]
    (with-open [w (writer output)]
      (.write w (wrap opts args)))))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-opts)]
    (cond
      (:help options) (exit 0 (usage summary))
      (>= (count arguments) 1) (dump options arguments)
      :else (exit 1 (error-msg errors)))))
