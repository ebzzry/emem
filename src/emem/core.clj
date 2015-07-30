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
   ["-r" nil "install the resource files only"
    :id :resonly]
   ["-R" nil "build the HTML file only"
    :id :htmlonly]
   ["-v" nil "increase verbosity"
    :id :verb
    :default 0
    ;; Use assoc-fn to create non-idempotent options
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-V" "--version" "display program version"]
   ["-h" "--help" "display this help"]])

;; (defn usage
;;   "Displays program usage."
;;   [summary]
;;   (->> ["Usage: emem [OPTION]... [MARKDOWN_FILE]..."
;;         ""
;;         "Options:"
;;         summary
;;         ]
;;        (s/join \newline)))

(defn display-usage
  "Displays program usage."
  [summary]
  (-> (->> ["Usage: emem [OPTION]... [MARKDOWN_FILE]..."
            ""
            "Options:"
            summary]
           (s/join \newline))
      println))

(defn error-msg
  "Displays the errors encountered during command parsing."
  [errors]
  (str "The following errors occurred while parsing your command:\n"
       (s/join \newline errors)))

(defn doexit
  ""
  [f & [code]]
  (f)
  (System/exit (or code 0)))

(defn msgexit
  "Exits the program with status code and message."
  [msg code]
  (doexit #(println msg) code))

(defn msg
  "Displays messages controlled by the verbosity option."
  [text req & [level]]
  (when (>= (or level 0) req)
    (println text)))

(defn version
  "Prints program version."
  []
  (msg "Printing program version ..." 1)
  (with-open [in (io/input-stream (io/resource "VERSION"))]
    (let [ver (slurp in)]
      (if *command-line-args*
        (spit "/dev/stdout" ver)
        (s/trim ver)))))

(defn doc-title
  "Returns the title of the document."
  [file opts]
  (msg "Retrieving document title ..." 1)
  (with-open [file (io/reader file)]
    (str (first (line-seq file)))))

(defn install-resources
  "Installs the files required by the HTML file."
  [opts]
  (msg "Installing resources ..." 1 (:verb opts))
  (let [res (io/resource "static")]
    (when (fs/exists? res)
      (doseq [[path uris] (cp/resources res)
              :let [uri (first uris)
                    relative-path (subs path 1)
                    path (str res "/" relative-path)]]
        (with-open [in (io/input-stream (io/resource path))]
          (io/make-parents path)
          (io/copy in (io/file path)))))))

(defn wrap
  "Wraps TEXT with HTML necessary for correct page display."
  [opts args text]
  (let [[lead & _] args
        title (or (or (:title opts) (:titlehead opts))
                  (doc-title lead opts))
        header (or (:header opts) (:titlehead opts))]
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

(defn mdify
  "Converts Markdown inputs to HTML strings."
  [opts args]
  ;; (msg "Loading input files ..." 1 (:verb opts))
  (wrap opts args
        (apply str (map #(md-to-html-string (slurp %)) args))))

(defn files-exist?
  "Returns true if all FILES exist."
  [files]
  (every? #(fs/exists? %) files))

(defn write-html
  "Writes the HTML file to disk."
  [opts args]
  (msg "Writing output files ..." 1 (:verb opts))
  (let [output (or (:output opts))]
    (with-open [out (io/output-stream output)]
      (spit out (mdify opts args)))))

(defn stage
  "Sets up the environment for arguments F and G."
  [opts args f g]
  (if (files-exist? args)
    (do (or (:htmlonly opts)
            (install-resources opts))
        (f))
    (g)))

(defn encode
  "Converts Markdown inputs to HTML."
  [output input & {:as opts}]
  (stage opts input
         #(write-html (merge {:output output} (dissoc opts :output))
                      input)
         #(identity nil)))

(defn launch
  "Converts Markdown inputs to HTML."
  [opts args errors summary]
  (stage opts args
         #(write-html opts args)
         #(doexit (display-usage summary)) 1))

(defn min-args?
  "Returns true if the minimum amount of command line input is met."
  [opts args]
  (> (count args) 0))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-opts)]
    (cond
      (:help options) (doexit #(display-usage summary))
      (:version options) (doexit #(version))
      (:resonly options) (doexit #(install-resources options))
      errors (msgexit (error-msg errors) 1))
    (and (min-args? options arguments)
         (launch options arguments errors summary))))
