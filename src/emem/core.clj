(ns emem.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [emem.utils :as u]
            [me.raynes.fs :as fs]
            [cpath-clj.core :as cp])
  (:use [markdown.core]
        [hiccup.core])
  (:import [java.util.zip
            GZIPInputStream]
           [java.io
            File
            ByteArrayInputStream])
  (:gen-class))

(def cli-opts
  "Specification for the command-line options."
  [["-o" "--output=HTML_FILE" "output file"
    :default "/dev/stdout"
    ]
   ["-t" "--title TITLE" "document title"]
   ["-H" "--header HEADER" "document header"]
   ["-T" "--titlehead TEXT" "like -t TEXT -H TEXT"]
   ["-v" nil "increase verbosity"
    :id :verb
    :default 0
    ;; Use assoc-fn to create non-idempotent options
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help"]])

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
  (str "The following errors occurred while parsing your command:\n\n"
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

(defn foo [path]
    (when path
      (-> (Thread/currentThread)
          .getContextClassLoader
          (.getResource path))))

(defn install-resources
  ""
  [base]
  ;; (fs/copy-dir (-> "static" io/resource io/file) fs/*cwd*)
  ;; (fs/copy-dir (-> "static" io/resource .getFile) fs/*cwd*)
  ;; (fs/copy-dir (-> "static" io/resource .getPath) fs/*cwd*)
  ;; (fs/copy-dir (foo "static") fs/*cwd*)
  ;; (io/copy (-> "static/css/custom.css" io/resource io/file) (io/file "."))
  ;; (io/copy (io/file (foo "static")) (io/file "."))
  ;; (println (cp/resources (io/resource "static")))
  ;; (doseq [[k v] (cp/resources (io/resource "static"))]
  ;;   ())
  ;; (fs/copy (.getPath (io/resource "test.txt")) fs/*cwd*)

  ;; (doseq [[k v] (cp/resources (io/resource "static"))]
  ;;   (fs/copy (str "static" k) fs/*cwd*))

  ;; (doseq [[path uris] (cp/resources (io/resource "static"))
  ;;         :let [uri (first uris)
  ;;               relative-path (subs path 1)
  ;;               output-file (io/file fs/*cwd* relative-path)]]
  ;;   (with-open [in (io/input-stream uri)]
  ;;     (io/copy in output-file)))

  ;; WORKS
  ;; (with-open [in (io/input-stream (io/resource "static/css/custom.css"))]
  ;;   (io/copy in (io/file "blah.foo")))

  ;; (doseq [[path uris] (cp/resources (io/resource "static"))
  ;;         :let [uri (first uris)
  ;;               relative-path (subs path 1)
  ;;               ;; out (io/file (File. "static") relative-path)
  ;;               out (io/resource (str "static" relative-path))]]
  ;;   ;; (println out)
  ;;   (println relative-path)
  ;;   (println (str "static/" relative-path)))

  ;; (let [path (str "static/" "ico/favicon.ico")]
  ;;   (with-open [in (io/input-stream (io/resource path))]
  ;;     (io/make-parents path)
  ;;     (io/copy in (io/file path))))

  (doseq [[path uris] (cp/resources (io/resource base))
          :let [uri (first uris)
                relative-path (subs path 1)
                path (str base "/" relative-path)]]
    (with-open [in (io/input-stream (io/resource path))]
      (io/make-parents path)
      (io/copy in (io/file path)))))

(defn html-wrapper
  ""
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
       [:link {:rel "stylesheet" :href "static/css/zenburn.css"}]
       [:script {:src "static/js/highlight.pack.js"}]
       [:script "hljs.initHighlightingOnLoad();"]]
      [:body {:id "body"}
       (if header [:h1 header] "")
       text]])))

(defn wrap
  ""
  [opts args]
  (msg "Loading input files ..." (:verb opts) 1)
  (html-wrapper opts args
                (apply str (map #(md-to-html-string (slurp %)) args))))

(defn dump
  ""
  [opts args]
  (msg "Writing output files ..." (:verb opts) 1)
  (let [output (or (:output opts))]
    (with-open [out (io/output-stream output)]
      (spit out (wrap opts args)))))  ;Try output-stream & spit?

(defn setup
  ""
  [opts args]
  (msg "Installing resources ..." (:verb opts) 1)
  (install-resources "static"))

(defn launch
  ""
  [opts args]
  (setup opts args)
  (dump opts args))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-opts)]
    (cond
      (:help options) (exit 0 (usage summary))
      (>= (count arguments) 1) (launch options arguments)
      :else (exit 1 (error-msg errors)))))
