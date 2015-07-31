(ns emem.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [emem.utils :as u]
            [me.raynes.fs :as fs]
            [cpath-clj.core :as cp])
  (:use [markdown.core]
        [hiccup.core])
  (:import [java.io File BufferedReader])
  (:gen-class))

(def default-output "/dev/stdout")

(def cli-opts
  "Specification for the command-line options."
  [["-o" "--output=HTML_FILE" "output file"
    :default default-output]
   ["-r" "--raw"            "emit raw HTML"]
   ["-b" "--bare"           "emit bare HTML"]
   ["-H" "--htmlonly"       "emit full HTML, sans resources"]
   ["-R" "--resonly"        "install the resource files only"]

   [nil "--title TEXT"     "document title"]
   [nil "--header TEXT"   "document header"]
   ["-T" "--titlehead TEXT" "like --title TEXT --header TEXT"]

   ["-v" nil                "increase verbosity"
    :id :verbosity
    :default 0
    ;; Use assoc-fn to create non-idempotent options
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-V" "--version" "display program version"]
   ["-h" "--help" "display this help"]])

(defn verb
  "Provides default value for :verbosity option."
  [opts]
  (or (:verbosity opts) 0))

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
  "Evaluates F, then exits to OS with CODE."
  [f & [code]]
  (f)
  (System/exit (or code 0)))

(defn msg-exit
  "Exits the program with status code and message."
  [msg code]
  (doexit #(println msg) code))

(defn msg
  "Displays messages controlled by the verbosity option."
  ([text]
   (msg text 1 0))
  ([text level]
   (msg text level 0))
  ([text level override]
   (when (>= override level)
     (println text))))

(defn resource-stream
  ""
  [res]
  (io/input-stream (io/resource res)))

(defn with-resource
  "Locates the resource files in the classpath."
  [res f dir]
  (doseq [[path _] (cp/resources (io/resource res))
          :let [relative-path (subs path 1)
                file (str res "/" relative-path)]]
    (f file (or dir fs/*cwd*))))

(defn version
  "Displays program version."
  []
  (let [f (fn [file dir]
            (with-open [in (resource-stream file)]
              (let [ver (slurp in)]
                (spit *out* ver))))]
    (with-resource "etc" f fs/*cwd*)))

(defn install-resources
  "Installs the files required by the HTML file."
  [opts]
  (msg "[*] Installing resources ..." 1 (verb opts))
  (let [dir (-> (:output opts)
                io/file .getAbsolutePath
                io/file .getParent
                io/file)]
    (let [f (fn [file dir]
              (with-open [in (resource-stream file)]
                (let [path (io/file dir file)]
                  (io/make-parents (io/file dir file))
                  (io/copy in (io/file dir file)))))]
      (with-resource "static" f dir))))

(defn doc-title
  "Returns the title of the document."
  [file opts]
  (with-open [file (io/reader file)]
    (str (first (line-seq file)))))

(defn claws
  "Returns the empty string if TEST evaluates to false; otherwise
returns THEN."
  [test then]
  (if test then ""))

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
       [:meta {:http-equiv "Content-Type"
               :content "text/html;charset=utf-8"}]
       (claws
        (not (:bare opts))
        (html
         [:link {:rel "shortcut icon"
                 :href "static/ico/favicon.ico"
                 :type "image/x-icon"}]
         [:link {:rel "stylesheet"
                 :href "static/css/custom.css"
                 :media "all"}]
         [:link {:rel "stylesheet"
                 :href "static/css/tomorrow-night.css"}]
         [:script {:src "static/js/highlight.pack.js"}]
         [:script "hljs.initHighlightingOnLoad();"]))]
      [:body
       (claws header [:h1 header])
       text]])))

(defn mdify
  "Converts Markdown inputs to HTML strings."
  [opts args]
  (let [s (apply str (map #(md-to-html-string (slurp %)) args))]
    (if (not (:raw opts))
      (wrap opts args s)
      s)))

(defn files-exist?
  "Returns true if all FILES exist."
  [files opts]
  (msg "[*] Verifying inputs ..." 1 (verb opts))
  (every? #(fs/exists? %) files))

(defn write-html
  "Writes the HTML file to disk."
  [opts args]
  (msg "[*] Writing output file ..." 1 (verb opts))
  (let [output (or (:output opts))]
    (with-open [out (io/output-stream output)]
      (spit out (mdify opts args)))))

(defn stage
  "Sets up the environment for F and G."
  [opts args f bail]
  (msg "[*] Setting up stage ..." 1 (verb opts))
  (if (:resonly opts)
    (install-resources opts)
    (if (files-exist? args opts)
      (do (or (or (:raw opts)
                  (:bare opts)
                  (:htmlonly opts)
                  (= (:output opts) default-output))
              (install-resources opts))
          (f))
      (bail))))

(defn produce
  "Produces HTML from Markdown inputs.

OUTPUT: regular file

INPUT: vector of markdown inputs

OPTIONS:
  :raw Boolean          emit raw HTML
  :bare Boolean         emit bare HTML
  :htmlonly Boolean     emit full HTML, sans resources
  :resonly Boolean      install the resource files only
  :title String         document title
  :header String        document header
  :titlehead String     like :title String :header String"
  [output input & {:as options}]
  (let [out {:output output}]
    (stage (merge options out)
           input
           #(write-html (merge out (dissoc options :output)) input)
           #(identity nil))))

(defn launch
  "Produces HTML from Markdown inputs."
  [opts args errors summary]
  (stage opts args
         #(write-html opts args)
         #(doexit (display-usage summary))))

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
      (:version options) (doexit version)
      (:resonly options) (doexit #(install-resources options))
      errors (msg-exit (error-msg errors) 1))
    (if (not-empty arguments)
      (launch options arguments errors summary)
      (let [temp (.getAbsolutePath (u/make-temp))
            in (slurp *in*)]
        (spit temp in)
        (launch options [temp] errors summary)
        (fs/delete temp)))))
