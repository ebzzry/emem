(ns emem.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [emem.utils :as u]
            [me.raynes.fs :as fs]
            [cpath-clj.core :as cp])
  (:use [markdown.core]
        [hiccup.core])
  (:import [java.io File])
  (:gen-class))

(def cli-opts
  "Specification for the command-line options."
  [["-o" "--output=HTML_FILE" "output file"
    :default "/dev/stdout"]
   ["-t" "--title TITLE" "document title"]
   ["-H" "--header HEADER" "document header"]
   ["-T" "--titlehead TEXT" "like -t TEXT -H TEXT"]
   ["-v" nil "increase verbosity"
    :id :verb
    :default 0
    ;; Use assoc-fn to create non-idempotent options
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-r" nil "install only the resource files; do not build the HTML files"
    :id :resourcesonly]
   ["-R" nil "do not install the resource files"
    :id :noresources]
   ["-h" "--help" "display this help"]])

(defn usage
  "Displays program usage."
  [opts-summary]
  (->> ["Usage: emem [OPTION]... MARKDOWN_FILE..."
        ""
        "Options:"
        opts-summary
        ]
       (s/join \newline)))

(defn error-msg
  "Displays the errors encountered during command parsing."
  [errors]
  (str "The following errors occurred while parsing your command:\n"
       (s/join \newline errors)))

(defn exit
  "Exits the program with status code and message."
  [status msg]
  (println msg)
  (System/exit status))

(defn doc-title
  "Returns the title of the document."
  [file]
  (with-open [file (io/reader file)]
    (str (first (line-seq file)))))

(defn msg
  "Displays messages controlled by the verbosity option."
  [text level req]
  (when (>= level req)
    (println text)))

(defn install-resources
  "Installs the files required by the HTML file(s)."
  [res opts args]
  (msg "Installing resources ..." (:verb opts) 1)
  (doseq [[path uris] (cp/resources (io/resource res))
          :let [uri (first uris)
                relative-path (subs path 1)
                path (str res "/" relative-path)]]
    (with-open [in (io/input-stream (io/resource path))]
      (io/make-parents path)
      (io/copy in (io/file path)))))

(defn wrap
  "Wraps TEXT with HTML necessary for correct page display."
  [opts args text]
  (let [[lead & _] args
        title (or (or (:title opts) (:titlehead opts))
                  (doc-title lead))
        header (or (:header opts)
                   (:titlehead opts))]
    (html
     [:html
      [:head
       [:title title]
       [:meta {:http-equiv "Content-Type" :content "text/html;charset=utf-8"}]
       [:link {:rel "shortcut icon" :href "static/ico/favicon.ico" :type "image/x-icon"}]
       [:link {:rel "stylesheet" :href "static/css/custom.css" :media "all"}]
       [:link {:rel "stylesheet" :href "static/css/tomorrow-night.css"}]
       [:script {:src "static/js/highlight.pack.js"}]
       [:script "hljs.initHighlightingOnLoad();"]]
      [:body
       (if header [:h1 header] "")
       text]])))

(defn md
  "Converts the markdown files to HTML strings."
  [opts args]
  (msg "Loading input files ..." (:verb opts) 1)
  (wrap opts args
        (apply str (map #(md-to-html-string (slurp %)) args))))

(defn build-html
  "Writes the HTML file to disk."
  [opts args]
  (msg "Writing output files ..." (:verb opts) 1)
  (let [output (or (:output opts))]
    (with-open [out (io/output-stream output)]
      (spit out (md opts args)))))

(defn launch
  "Performs the top-level calls that does the actual stuff."
  [opts args]
  (let [res "static"]
    (if (:resourcesonly opts)
      (install-resources res opts args)
      (do (or (:noresources opts) (install-resources res opts args))
          (build-html opts args)))))

(defn -main
  "Defines the entry point."
  [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-opts)]
    (cond
      (:help options) (exit 0 (usage summary))
      ;; (< (count arguments) 1) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors)))
    (launch options arguments)))
