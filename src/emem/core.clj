(ns emem.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [markdown.core :as md]
            [hiccup.core :as hi])
  (:use emem.util emem.html)
  (:import [java.io File BufferedReader ByteArrayOutputStream]
           [java.lang String]
           [clojure.lang PersistentArrayMap PersistentVector])
  (:gen-class))

(def ^:private cli-opts
  "Specification for the command-line options."
  [["-o" "--output HTML"              "specify output file (default: file basename sans extension + .html)" :id :out]
   ["-d" "--directory DIRECTORY"      "specify output directory (default: file directory)" :id :dir]
   ["-r" "--resources"                "build the resource files only" :id :resources]
   ["-R" "--no-resources"             "build HTML output sans resources"]
   ["-c" "--continuous"               "run in continuous build mode"]
   ["-t" "--refresh MILLISECONDS"     "time between rebuilds (default: 200 ms)"]
   ["-s" "--standalone"               "embed both the CSS data and JS external reference"]
   ["-a" "--standalone-css"           "embed only the CSS data"]
   ["-f" "--full-width"               "use full page width" :id :full]
   ["-i" "--icon"                     "use the included favicon"]
   ["-w" "--raw"                      "emit 1:1 Markdown-HTML equivalence-don't build a complete HTML document"]
   ["-p" "--plain"                    "build plain HTML-don't use CSS and JS"]
   ["-m" "--merge"                    "merge and process the inputs into a single output"]
   ["-l" "--lang"                     "specify document language (default: en)"]
   ["-u" "--use-root"                 "use the root path for the resources instead of current directory"]
   ["-I" "--title TEXT"               "specify document title (default: file basename)"]
   ["-E" "--header TEXT"              "specify document header (default: none)"]
   ["-T" "--titlehead TEXT"           "the same as --title TEXT --header TEXT"]
   ["-F" "--first-line"               "use first line of file as document title"]
   ["-H" "--head CONTENT"             "insert arbitrary content in the head tag"]
   ["-D" "--description TEXT"         "specify meta tag description attribute value"]
   ["-K" "--keywords TEXT"            "specify meta tag keywords attribute value"]
   [nil "--og-title VALUE"            "specify OGP title attribute value"]
   [nil "--og-type VALUE"             "specify OGP type attribute value"]
   [nil "--og-url VALUE"              "specify OGP url attribute value"]
   [nil "--og-image VALUE"            "specify OGP image attribute value"]
   [nil "--og-description VALUE"      "specify OGP description attribute value"]
   [nil "--og-determiner VALUE"       "specify OGP determiner attribute value"]
   [nil "--og-locale VALUE"           "specify OGP locale attribute value"]
   [nil "--og-locale-alternate VALUE" "specify OGP locale:alternate attribute value"]
   [nil "--og-site-name VALUE"        "specify OGP site_name attribute value"]
   [nil "--og-audio VALUE"            "specify OGP audio attribute value"]
   [nil "--og-video VALUE"            "specify OGP video attribute value"]
   ["-M" "--css CSS"                  "specify alternative main CSS"]
   ["-C" "--inline-css CSS"           "specify inline CSS" :id :inline]
   ["-S" "--style STYLE"              "specify alternative style for the syntax highlighter"]
   ["-L" "--list-styles"              "list available styles for the syntax highlighter"]
   ["-A" "--analytics CODE"           "specify Google Analytics code (UA-XXXXXXXX-X)"]
   ["-v" nil                          "increase verbosity"
    :id :verbosity :default 0 :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-V" "--version" "display program version"]
   ["-h" "--help"    "display this help"]])

(defn- display-usage
  "Display program usage."
  [text]
  (println
   (s/join \newline
           ["Usage: emem [OPTION]... [MARKDOWN_FILE]..."
            ""
            "Options:"
            text
            ""
            "See https://github.com/ebzzry/emem for more information"])))

(defn- display-errors
  "Display the errors encountered during command parsing."
  [errors]
  (println
   (str "The following errors occurred:\n\n"
        (s/join \newline errors))))

(defn- version
  "Display the program version."
  []
  (with-open [in (find-resource "etc/VERSION")]
    (let [ver (slurp in)]
      (spit *out* ver))))

(defn- get-styles
  "Return the available styles for the syntax highlighter."
  []
  (sort compare (remove #{"main" "ewan"}
                 (map root (get-resources "static/css")))))

(defn- list-styles
  "Display the available styles for the syntax highlighter."
  []
  (doseq [style (get-styles)]
    (println style)))

(defn- inputs-ok?
  "Verify that all inputs exist."
  [inputs opts]
  (msg "[*] Verifying inputs..." 1 (verb opts))
  (or (when-let [[in & _] inputs]
        (= in *in*))
      (files-exist? inputs)))

(defn- write-html
  "Write the HTML to file."
  [opts args]
  (msg "[*] Writing output..." 1 (verb opts))
  (let [output (out opts)
        f (fn [out]
            (.write out (html opts args))
            (flush))]
    (if (= output *out*)
      (f *out*)
      (with-open [out (io/writer output)]
        (f out)))))

(defn- write-pdf [] nil)

(defn- stage
  "Verify options and conditionalizes installation of resources."
  [opts args f exit]
  (msg "[*] Setting up stage..." 1 (verb opts))
  (cond
    (:resources opts) (copy-resources opts)

    (inputs-ok? args opts)
    (do (or (:raw opts)
            (:plain opts)
            (:no-resources opts)
            (in? args)
            (= (:out opts) "-")
            (= (:out opts) *out*)
            (copy-resources opts args))
        (f))

    :else (exit)))

(declare launch)

(defn- rebuild
  "Rebuild the target if any of the input files are modified."
  [opts args]
  (msg "[*] Rebuilding..." 1 (verb opts))
  (let [times (mod-times args)
        refresh (if-let [t (:refresh opts)] (read-string t) 200)
        options (merge-true opts :no-resources)]
    (with-out-str (print times))
    (Thread/sleep refresh)
    (if (not= times (mod-times args))
      (launch options args)
      (recur opts args))))

(defn html-name-path
  "Return absolute path to HTML file"
  [dest input]
  (html-name (str (absolute-path dest) "/" (basename input))))

(defn- launch
  "Convert a Markdown file to HTML."
  [opts args]
  (let [input (first args)
        options (merge-options
                 opts
                 (or
                  (let [dest (:dir opts)]
                    (cond
                      ;; directory does not exist, create it
                      (and dest (not (exists? dest)))
                      (do (create-directory (absolute-path dest))
                          (html-name-path dest input))

                      ;; directory exists
                      (and dest (exists? dest) (directory? dest))
                      (html-name-path dest input)

                      ;; regular file exists
                      (and dest (exists? dest) (not (directory? dest)))
                      (html-name input)

                      ;; :dir is not used
                      :else
                      (html-name input)))
                  *out*))
        f (fn [inputs]
            (stage options inputs
                   #(write-html options inputs)
                   #(exit)))]
    (if (empty? args)
      (let [out (:out opts)]
        (if (and out (not (out? opts)) (not (= "-" out)))
          (do (or (:no-resources opts)
                  (when-not (:standalone opts)
                    (copy-resources opts)))
              (f [*in*]))
          (f [*in*])))
      (do (f args)
          (and (:continuous opts)
               (rebuild opts args))))))

(defn- multi-launch
  "Invoke LAUNCH on ARGS. If either :continuous and :no-resources, or
  if force is true, parallelize LAUNCH on arguments."
  [opts args]
  (if (empty? args)
    (launch opts args)
    (doseq [arg args]
      (if (and (:continuous opts)
               (:no-resources opts))
        (future (launch opts [arg]))
        (launch opts [arg])))))

(defn- expand-md
  "Return a vector of absolute paths of Markdown files, found in
  traversing PATHS, including directories."
  [paths]
  (expand paths "md"))

(defn- dump
  "Convert Markdown inputs to HTML."
  [opts args]
  (let [argsn (count args)
        args? (> argsn 1)
        xargs (expand-md args)]
    (cond
      ;; install resources
      (:resources opts)
      (exit #(install-resources))

      ;; merge
      (and args? (:merge opts))
      (launch opts xargs)

      ;; multi parallel
      (or (and args? (common-directory? args))
          (and (= argsn 1)) (directory? (first args)))
      (let [dir (if (:out opts)
                  (abs-parent (:out opts))
                  (abs-parent (first args)))]
        (or (:no-resources opts)
            (when-not (:standalone opts)
              (copy-resources (merge-options opts dir))))
        (multi-launch (merge-true opts :no-resources)
                      xargs))

      ;; multi serial
      args?
      (multi-launch opts xargs)

      :else
      (multi-launch opts xargs))))

(defn convert
  "Convert Markdown inputs to HTML.

Options:
  :out String                 specify output file
  :directory String           specify output directory
  :resources Boolean          build the resource files only
  :no-resources Boolean       build HTML output sans resources
  :standalone Boolean         embed both the CSS data and JS external reference
  :standalone-css Boolean     embed only the CSS data
  :full Boolean               use full page width
  :icon Boolean               use the included favicon
  :raw Boolean                emit 1:1 Markdown-HTML equivalence-don't build a complete HTML document
  :plain Boolean              build plain HTML-don't use CSS and JS
  :merge Boolean              merge and process the files into one file
  :lang String                specify document language
  :use-root Boolean           use the root path for the resources instead of current directory
  :title String               specify document title
  :header String              specify document header
  :titlehead String           the same as :title String :header String
  :first-line                 use first line of file as document title
  :head String                insert arbitrary content in the head tag
  :description String         specify meta tag description attribute value
  :keywords String            specify meta tag keywords attribute value
  :og-title String            specify OGP title attribute value
  :og-type String             specify OGP type attribute value
  :og-url String              specify OGP url attribute value
  :og-image String            specify OGP image attribute value
  :og-description String      specify OPG description attribute value
  :og-determiner String       specify OGP determiner attribute value
  :og-locale String           specify OGP locale attribute value
  :og-locale-alternate String specify OGP locale:alternate attribute value
  :og-site-name String        specify OGP site_name attribute value
  :og-audio String            specify OGP audio attribute value
  :og-video String            specify OGP video attribute value
  :css String                 specify alternative main CSS resource
  :inline String              specify inline CSS
  :style String               specify alternative style for the syntax highlighter
  :analytics String           specify Google Analytics code"
  [in & args]
  (cond
    ;; (convert "README.md")
    (and (zero? (count args)) (string? in))
    (convert [in] :out (html-name in))

    ;; (convert "README.md" "foo.html")
    (and (= (count args) 1) (every? string? [in (first args)]))
    (convert [in] :out (first args))

    ;; (convert ["foo.md" "bar.md" "baz.md"]
    ;;          ["mu.html" "ka.html" "mo.html"])
    (and (vector? in) (vector? (first args)))
    (let [v1 in
          v2 (first args)]
      (and (= (count v1) (count v2))
           (doseq [[md html] (zipmap v1 v2)]
             (convert md html))))

    ;; (convert ["README.md" "TODO.md"])
    ;; (convert [] ...)
    (vector? in)
    (let [options (apply sorted-map args)]
      (dump options in))))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-opts)]
    (cond
      errors (exit #(display-errors errors) 1)
      (:help options) (exit #(display-usage summary))
      (:version options) (exit version)
      (:list-styles options) (exit list-styles)

      :else (dump options arguments))))
