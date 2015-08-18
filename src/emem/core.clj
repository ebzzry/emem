(ns emem.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [markdown.core :as md]
            [hiccup.core :as hi]
            [emem.util :as u])
  (:import [java.io File BufferedReader ByteArrayOutputStream]
           [java.lang String]
           [clojure.lang PersistentArrayMap PersistentVector])
  (:gen-class))

(def ^:private cli-opts
  "Specification for the command-line options."
  [["-o" "--output HTML"  "output file" :id :out]
   ["-r" "--resources"    "install the resource files only"]
   ["-n" "--no-resources" "build full HTML; don't install resources"]

   ["-m" "--multi"               "enable multiple input processing"]
   ["-d" "--directory DIRECTORY" "process .md files in directory; implies -m"]
   ["-c" "--continuous"           "run in continuous build mode"]
   ["-f" "--refresh MILLISECONDS" "time between rebuilds (default: 200)"]

   ["-w" "--raw"   "emit 1:1 Markdown-HTML equivalence"]
   ["-p" "--plain" "build plain HTML; don't use CSS and JS"]
   ["-M" "--merge" "merge and process the files into a single output"]

   [nil "--title TEXT"      "document title"]
   [nil "--header TEXT"     "document header"]
   ["-t" "--titlehead TEXT" "the same as --title TEXT --header TEXT"]

   ["-I" "--favicon ICO" "favicon resource"]
   ["-C" "--css CSS"     "CSS resource"]
   ["-S" "--style STYLE" "style id for the syntax highlighter"]
   ["-L" "--list-styles" "list available styles for the syntax highlighter"]

   ["-v" nil         "increase verbosity"
    :id :verbosity :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-V" "--version" "display program version"]
   ["-h" "--help"    "display this help"]])

(def ^:private default-style
  "Default style for the syntax highlighter."
  "ewan")

(defn- verb
  "Provides default value for :verbosity option."
  [opts]
  (or (:verbosity opts) 0))

(defn- display-usage
  "Displays program usage."
  [text]
  (println
   (s/join \newline
           ["Usage: emem [OPTION]... [MARKDOWN_FILE]..."
            ""
            "Options:"
            text])))

(defn- display-errors
  "Displays the errors encountered during command parsing."
  [errors]
  (println
   (str "The following errors occurred:\n\n"
        (s/join \newline errors))))

(defn version
  "Displays program version."
  []
  (with-open [in (u/find-resource "etc/VERSION")]
    (let [ver (slurp in)]
      (spit *out* ver))))

(defn get-styles
  "Returns the available styles for the syntax highlighter."
  []
  (sort compare (remove #{"main" "ewan"}
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
  [opts & [args]]
  (u/msg "[*] Installing resources..." 1 (verb opts))
  (let [dir (-> (:out opts) u/parent* io/file)
        f (fn [file dir]
            (with-open [in (u/re-stream file)]
              (let [path (io/file dir file)]
                (io/make-parents (io/file dir file))
                (io/copy in (io/file dir file)))))]
    (with-resources "static" f dir)))

(defn install-resources
  "Installs the HTML resources relative to PATH."
  ([]
   (install-resources (u/pwd)))
  ([path]
   (copy-resources {:out (or path (u/pwd))})))

(defn- inputs-ok?
  "Verifies that all inputs exist."
  [inputs opts]
  (u/msg "[*] Verifying inputs..." 1 (verb opts))
  (or (when-let [[in & _] inputs]
        (= in *in*))
      (u/files-exist? inputs)))

(defn- html-page
  "Wraps TEXT with HTML necessary for correct page display."
  [opts args text]
  (let [title (or (:title opts)
                  (:titlehead opts)
                  (when (u/in? args) "")
                  (when-let [line (u/first-line (first args))]
                    line))
        header (or (:header opts) (:titlehead opts))
        favicon (or (:favicon opts) "static/ico/glider.ico")
        css (or (:css opts) "static/css/main.css")
        style (str "static/css/"
                   (or (:style opts) default-style)
                   ".css")]
    (hi/html
     [:html
      [:head
       (u/quo title [:title title])
       [:meta {:http-equiv "Content-Type"
               :content "text/html;charset=utf-8"}]
       ;; u/quo
       (when (not (:plain opts))
         (hi/html
          [:link {:rel "icon" :href favicon :type "image/x-icon"}]
          [:link {:rel "stylesheet" :href css :media "all"}]
          (when-not (= (:style opts) "-")
            (hi/html
             [:link {:rel "stylesheet" :href style :media "all"}]
             [:script {:src "static/js/highlight.pack.js"}]
             [:script "hljs.initHighlightingOnLoad();"]))))]
      [:body
       (u/quo header [:h1 header])
       text]])))

(defn markdown
  "Returns a Markdown string converted to HTML."
  [str]
  (md/md-to-html-string str))

(defn- html
  "Converts Markdown inputs to HTML strings."
  [opts args]
  (let [text (if (:merge opts)
               (s/join (map #(markdown (slurp %)) args))
               (markdown (slurp (first args))))]
    (if (:raw opts)
      text
      (html-page opts args text))))

(defn html-name
  "Returns the HTML name of PATH"
  [path]
  (str (u/abs-file-name path) ".html"))

(defn write-html
  "Writes the HTML to file."
  [opts args]
  (u/msg "[*] Writing output..." 1 (verb opts))
  (let [output (u/out opts)
        ;; output (:out opts)
        f (fn [out]
            (.write out (html opts args))
            (flush))]
    (if (= output *out*)
      (f *out*)
      (with-open [out (io/writer output)]
        (f out)))))

(defn- stage
  "FIXME: better doc"
  [opts args f exit]
  (u/msg "[*] Setting up stage..." 1 (verb opts))
  (cond
    (:resources opts) (copy-resources opts)

    (inputs-ok? args opts)
    (do (or (:raw opts)
            (:plain opts)
            (:no-resources opts)
            (u/in? args)
            (= (:out opts) "-")
            (= (:out opts) *out*)
            (copy-resources opts args))
        (f))
    
    :else (exit)))

(defn convert
  "Converts Markdown inputs to HTML.

ARGS: Markdown string, or vector of Markdown files

OPTS:
  :out String           output file
  :resources Boolean    install the resource files only
  :no-resources Boolean build full HTML; don't install resources
  :directory String     process .md files in directory
  :raw Boolean          emit 1:1 Markdown-HTML equivalence
  :plain Boolean        build plain HTML; don't use CSS and JS
  :merge Boolean        merge and process the files into one file
  :title String         document title
  :header String        document header
  :titlehead String     the same as :title String :header String
  :favicon String       favicon resource
  :css String           CSS resource
  :style String         style id for the syntax highlighter
"
  ([arg]
   (cond
     (string? arg) (convert [arg] :out (html-name arg))
     (vector? arg) (convert arg :out *out*)
     :else nil))
  ([^PersistentVector args & {:as opts}]
   (let [options (u/merge-options opts)]
     (stage options args
            #(write-html options args)
            #(identity nil)))))

(declare launch)

(defn- rebuild
  "Rebuilds the target if any of the input files are modified."
  [opts args text]
  (u/msg "[*] Rebuilding..." 1 (verb opts))
  (let [times (u/mod-times args)
        refresh (if-let [t (:refresh opts)] (read-string t) 200)
        options (merge opts {:no-resources true})]
    (with-out-str (print times))
    (Thread/sleep refresh)
    (if (not= times (u/mod-times args))
      (launch options args text)
      (recur opts args text))))

(defn- launch
  "Converts a Markdown file to HTML."
  [opts args text]
  (let [options (u/merge-options opts (html-name (first args)))
        f (fn [inputs]
            (stage options inputs
                   #(write-html options inputs)
                   #(u/exit (display-usage text))))]
    (if (empty? args)
      (f [*in*])
      (do (f args)
          (and (:continuous opts)
               (rebuild opts args text))))))

(defn multi-launch
  "Invoke LAUNCH on ARGS."
  [opts args text & [force]]
  (doseq [arg args]
    (if (or (and (:continuous opts)
                 (:no-resources opts))
            force)
      (future (launch opts [arg] text))
      (launch opts [arg] text))))

(defn directory-launch
  "Invoke LAUNCH for all .md files in path."
  [opts args text]
  (let [options (merge opts {:multi true})
        files (vec (u/list-names-ext (:directory opts) ".md"))]
    (install-resources (:directory opts))

    ;; merge files and args?
    (multi-launch options files text true)
    (and (not-empty args)
         (multi-launch options args text))))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-opts)
        args-count (count arguments)
        args? (> args-count 1)]
    (cond
      (or (:help options)
          (and args? (not (:merge options)) (not (:multi options))))
      (u/exit #(display-usage summary))
      
      (:version options) (u/exit version)
      (:styles options) (u/exit list-styles)

      (:directory options) (directory-launch options arguments summary)
      (:resources options) (u/exit install-resources (:dir options))

      (and args? (:multi options) (not (:merge options)))
      (multi-launch options arguments summary)

      errors (u/exit #(display-errors errors) 1)
      :else (launch options arguments summary))))

