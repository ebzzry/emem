(ns emem.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [markdown.core :as md]
            [hiccup.core :as hi]
            [me.raynes.fs :as fs]
            [cpath-clj.core :as cp]
            [emem.util :as u])
  (:import [java.io File BufferedReader])
  (:gen-class))

(def ^:private default-output "/dev/stdout")

(def ^:private cli-opts
  "Specification for the command-line options."
  [["-o" "--output=HTML_FILE" "output file"
    :default default-output]
   ["-r" "--raw"            "emit raw HTML"]
   ["-b" "--bare"           "emit bare HTML"]
   ["-H" "--htmlonly"       "emit full HTML, sans resources"]
   ["-R" "--resonly"        "install the resource files only"]

   [nil "--title TEXT"      "document title"]
   [nil "--header TEXT"     "document header"]
   ["-T" "--titlehead TEXT" "like --title TEXT --header TEXT"]

   ["-v" nil                "increase verbosity"
    :id :verbosity
    :default 0
    ;; Use assoc-fn to create non-idempotent options
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-V" "--version" "display program version"]
   ["-h" "--help" "display this help"]])

(defn- verb
  "Provides default value for :verbosity option."
  [opts]
  (or (:verbosity opts) 0))

(defn- display-usage
  "Displays program usage."
  [summary]
  (-> (->> ["Usage: emem [OPTION]... [MARKDOWN_FILE]..."
            ""
            "Options:"
            summary]
           (s/join \newline))
      println))

(defn- error-msg
  "Displays the errors encountered during command parsing."
  [errors]
  (str "The following errors occurred:"
       (s/join \newline errors)))

(defn- with-resource
  "Locates the resource files in the classpath."
  [res f dir]
  (doseq [[path _] (cp/resources (io/resource res))
          :let [relative-path (subs path 1)
                file (str res "/" relative-path)]]
    (f file (or dir (u/pwd)))))

(defn version
  "Displays program version."
  []
  (let [f (fn [file dir]
            (with-open [in (u/restream file)]
              (let [ver (slurp in)]
                (spit *out* ver))))]
    (with-resource "etc" f (u/pwd))))

(defn- install-resources
  "Installs the files required by the HTML file."
  [opts]
  (u/msg "[*] Installing resources ..." 1 (verb opts))
  (let [dir (-> (:output opts)
                io/file u/abspath
                io/file u/parent
                io/file)]
    (let [f (fn [file dir]
              (with-open [in (u/restream file)]
                (let [path (io/file dir file)]
                  (io/make-parents (io/file dir file))
                  (io/copy in (io/file dir file)))))]
      (with-resource "static" f dir))))

(defn- html-page
  "Wraps TEXT with HTML necessary for correct page display."
  [opts args text]
  (let [[lead & _] args
        title (or (or (:title opts) (:titlehead opts))
                  (u/first-line lead))
        header (or (:header opts) (:titlehead opts))]
    (hi/html
     [:html
      [:head
       [:title title]
       [:meta {:http-equiv "Content-Type"
               :content "text/html;charset=utf-8"}]
       (u/quo
        (not (:bare opts))
        (hi/html
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
       (u/quo header [:h1 header])
       text]])))

(defn- html
  "Converts Markdown inputs to HTML strings."
  [opts args]
  (let [s (apply str (map #(md/md-to-html-string (slurp %)) args))]
    (if (not (:raw opts))
      (html-page opts args s)
      s)))

(defn- files-exist?
  "Verifies that all FILES exist."
  [files & [opts]]
  (u/msg "[*] Verifying inputs ..." 1 (verb opts))
  (u/files-ok? files))

(defn- write-html
  "Writes the HTML to file."
  [opts args]
  (u/msg "[*] Writing output file ..." 1 (verb opts))
  (let [output (or (:output opts))]
    (with-open [out (io/output-stream output)]
      (spit out (html opts args)))))

(defn- stage
  "Sets up the environment for F and BAIL."
  [opts args f exit]
  (u/msg "[*] Setting up stage ..." 1 (verb opts))
  (if (:resonly opts)
    (install-resources opts)
    (if (files-exist? args opts)
      (do (or (or (:raw opts)
                  (:bare opts)
                  (:htmlonly opts)
                  (= (:output opts) default-output))
              (install-resources opts))
          (f))
      (exit))))

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
           #(u/id nil))))

(defn- launch
  "Produces HTML from Markdown argss."
  [options arguments errors summary]
  (let [args (u/tempv arguments)
        f (fn []
            (stage options args
                   #(write-html options args)
                   #(u/ex (display-usage summary))))]
    (if (not-empty arguments)
      (f)
      (let [in (slurp *in*)
            temp (first args)]
        (spit temp in)
        (f)
        (u/delete temp)))))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-opts)]
    (cond
      (:help options) (u/ex #(display-usage summary))
      (:version options) (u/ex version)
      (:resonly options) (u/ex #(install-resources options))
      errors (u/bye (error-msg errors) 1))
    (launch options arguments errors summary)))
