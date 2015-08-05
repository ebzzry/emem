(ns emem.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [markdown.core :as md]
            [hiccup.core :as hi]
            [emem.util :as u])
  (:import [java.io File BufferedReader]
           [java.util Date]
           [clojure.lang
            PersistentArrayMap
            PersistentVector])
  (:gen-class))

(def ^:private cli-opts
  "Specification for the command-line options."
  [["-o" "--output HTML"       "specify output file" :id :out]

   ["-w" "--raw"               "emit raw HTML; 1:1 Markdown-HTML equivalent"]
   ["-p" "--plain"             "build plain HTML; don't use CSS and JS"]
   ["-n" "--nores"             "build full HTML; don't install the resources"]
   ["-R" "--resonly"           "install the resource files only"]

   ["-c" "--continuous"        "run in continuous build mode"]
   ["-r" "--refresh SECONDS"   "specify time between rebuilds"]

   ["-M" "--css-main CSS"      "specify CSS resource for body"]
   ["-C" "--css-code NAME"     "specify CSS for the syntax highlighter"]
   ["-L" "--styles"            "list available styles for the syntax highlighter"]

   [nil "--title TEXT"         "specify document title"]
   [nil "--header TEXT"        "specify document header"]
   ["-T" "--titlehead TEXT"    "like --title TEXT --header TEXT"]

   ["-v" nil                   "increase verbosity"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-V" "--version"           "display program version"]
   ["-h" "--help"              "display this help"]])

(def ^:private default-style
  "Default style for the syntax highlighter."
  "tomorrow-night")

(defn- verb
  "Provides default value for :verbosity option."
  [opts]
  (or (:verbosity opts) 0))

(defn- usage
  "Displays program usage."
  [text]
  (-> (->> ["Usage: emem [OPTION]... [MARKDOWN_FILE]..."
            ""
            "Options:"
            text]
           (s/join \newline))
      println))

(defn- error-msg
  "Displays the errors encountered during command parsing."
  [errors]
  (str "The following errors occurred:"
       (s/join \newline errors)))

(defn version
  "Displays program version."
  []
  (with-open [in (u/find-resource "etc/VERSION")]
    (let [ver (slurp in)]
      (spit *out* ver))))

(defn get-styles
  "Returns the available styles for the syntax highlighter."
  []
  (sort compare (remove #{"main"}
                 (map u/root (u/get-resources "static/css")))))

(defn list-styles
  "Displays the available styles for the syntax highlighter."
  []
  (doseq [style (get-styles)]
    (println style)))

(defn- with-resources
  "Locates the resource files in the classpath."
  [res f dir]
  (doseq [[path _] (u/resources res)]
    (let [relative-path (subs path 1)
          file (str res "/" relative-path)]
      (f file (or dir (u/pwd))))))

(defn- copy-resources
  "Installs the files required by the HTML file."
  [opts]
  (u/msg "[*] Installing resources ..." 1 (verb opts))
  (let [dir (-> (:out opts)
                io/file u/abspath
                io/file u/parent
                io/file)
        f (fn [file dir]
            (with-open [in (u/re-stream file)]
              (let [path (io/file dir file)]
                (io/make-parents (io/file dir file))
                (io/copy in (io/file dir file)))))]
    (with-resources "static" f dir)))

(defn re-install
  "Installs the HTML resources relative to PATH."
  [path]
  (copy-resources {:out path}))

(defn- inputs-ok?
  "Verifies that all inputs exist."
  [inputs opts]
  (u/msg "[*] Verifying inputs ..." 1 (verb opts))
  (or (when-let [[in & _] inputs]
        (= in *in*))
      (u/files-exist? inputs)))

(defn- html-page
  "Wraps TEXT with HTML necessary for correct page display."
  [opts args text]
  (let [title (or (:title opts)
                  (:titlehead opts)
                  (when-let [line (u/first-line (first args))]
                    line))
        header (or (:header opts) (:titlehead opts))
        css-main (or (:css-main opts) "static/css/main.css")
        css-code (str "static/css/"
                   (or (:css-code opts) default-style)
                   ".css")]
    (hi/html
     [:html
      [:head
       (u/quo title [:title title])
       [:meta {:http-equiv "Content-Type"
               :content "text/html;charset=utf-8"}]
       (u/quo
        (not (:plain opts))
        (hi/html
         [:link {:rel "shortcut icon"
                 :href "static/ico/favicon.ico"
                 :type "image/x-icon"}]
         [:link {:rel "stylesheet" :href css-main :media "all"}]
         [:link {:rel "stylesheet" :href css-code :media "all"}]
         [:script {:src "static/js/highlight.pack.js"}]
         [:script "hljs.initHighlightingOnLoad();"]))]
      [:body
       (u/quo header [:h1 header])
       text]])))

(defn- markdown
  "Returns a Markdown string converted to HTML."
  [str]
  (md/md-to-html-string str))

(defn- html
  "Converts Markdown inputs to HTML strings."
  [opts args]
  (let [text (apply str (map #(markdown (slurp %))
                             args))]
    (if (:raw opts)
      text
      (html-page opts args text))))

(defn write-html
  "Writes the HTML to file."
  [opts args]
  (u/msg "[*] Writing output ..." 1 (verb opts))
  (let [output (:out opts)
        f (fn [out]
            (.write out (html opts args))
            (flush))]
    (if (= output *out*)
      (f *out*)
      (with-open [out (io/writer output)]
        (f out)))))

(defn- stage
  "Sets up the environment for F and BAIL."
  [opts args f exit]
  (u/msg "[*] Setting up stage ..." 1 (verb opts))
  (if (:resonly opts)
    (copy-resources opts)
    (if (inputs-ok? args opts)
      (do (or (or (:raw opts)
                  (:plain opts)
                  (:nores opts)
                  (= (:out opts) *out*))
              (copy-resources opts))
          (f))
      (exit))))

(defn convert
  "Converts Markdown inputs to HTML.

ARGS: Markdown string, or vector of Markdown files

OPTS:
  :out String           specify output file
  :raw Boolean          emit raw HTML; 1:1 Markdown-HTML equivalent
  :plain Boolean        build plain HTML; don't use CSS and JS
  :nores Boolean        build full HTML; don't install the resources
  :resonly Boolean      install the resource files only
  :css-main String      specify CSS resource for body
  :css-code String      specify CSS for the syntax highlighter
  :title String         specify document title
  :header String        specify document header
  :titlehead String     like :title String :header String"
  ([args]
   (cond
     (string? args) (markdown args)
     (vector? args) (convert args :out *out*)
     :else nil))
  ([^PersistentVector args & {:as opts}]
   (let [options (u/out opts)]
     (stage options args
            #(write-html options args)
            #(identity nil)))))

(declare launch)

(defn- rebuild
  "Rebuilds the target if any of the input files are modified."
  [opts args text]
  (u/msg "[*] Rebuilding ..." 1 (verb opts))
  (let [times (u/modtimes args)
        refresh (let [t (:refresh opts) s 1000]
                  (if t (* (read-string t) s) s))
        options (merge opts {:nores true})]
    (with-out-str (print times))
    (Thread/sleep refresh)
    (if (not= times (u/modtimes args))
      (launch options args text)
      (recur opts args text))))

(defn- launch
  "Converts Markdown inputs to HTML."
  [opts args text]
  (let [options (u/out opts)
        f (fn [inputs]
            (stage options inputs
                   #(write-html options inputs)
                   #(u/exit (usage text))))]
    (if (empty? args)
      (f [*in*])
      (do (f args)
          (and (:continuous opts)
               (rebuild opts args text))))))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-opts)]
    (cond
      (:help options) (u/exit #(usage summary))
      (:version options) (u/exit version)
      (:styles options) (u/exit list-styles)
      (:resonly options) (u/exit #(copy-resources options))
      errors (u/bye (error-msg errors) 1))
    (launch options arguments summary)))
