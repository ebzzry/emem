(ns emem.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as s]
            [clojure.data.codec.base64 :as b64]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs])
  (:use [clojure.java.io]
        [markdown.core]
        [hiccup.core]
        [emem.resources])
  (:import [java.util.zip
            GZIPInputStream]
           [java.io
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
  (with-open [file (reader file)]
    (str (first (line-seq file)))))

(defn msg
  "Displays messages controlled by the verbosity option."
  [text level req]
  (when (>= level req)
    (println text)))

(defn string-input-stream
  "Returns a ByteArrayInputStream for the given String."
  ([^String s]
     (ByteArrayInputStream. (.getBytes s)))
  ([^String s encoding]
     (ByteArrayInputStream. (.getBytes s encoding))))

(defn base64-encode
  "Base64-encode the file in SRC; output to DEST."
  [^String src ^String dest]
  (with-open [in (io/input-stream src)
              out (io/output-stream dest)]
    (b64/encoding-transfer in out)))

(defn base64-decode
  "Base64-decode the file in SRC; output to DEST."
  [src dest]
  (with-open [in (io/input-stream src)
              out (io/output-stream dest)]
    (b64/decoding-transfer in out)))

(defn make-temp
  ""
  []
  (java.io.File/createTempFile "/tmp" ""))

(defn base64-decode-temp
  ""
  [src]
  (let [temp (make-temp)]
    (base64-decode (string-input-stream src) temp)
    (.getAbsolutePath temp)))

(defn gunzip
  "Decompresses INPUT with GZIP, then writes to OUTPUT.

WARNING: This does not handle binary files."
  [input output]
  (with-open [in (-> input io/input-stream GZIPInputStream.)
              out (io/output-stream output)]
    (spit output (slurp in))
    (delete-file input)))

(defn base64->gunzip
  "Consumes a base64 string INPUT, decompresses using GZIP,
then writes to OUTPUT."
  [input output]
  (gunzip (base64-decode-temp input) output))

(defn base64-string->gunzip
  "Consumes a base64 string INPUT read from input, decompresses using GZIP,
then writes to OUTPUT."
  [input output]
  (base64->gunzip (eval (read-string input)) output))

(defn resources-ids
  ""
  []
  (let [maps (map (comp str first) (ns-publics 'emem.resources))]
    maps))

(defn resource->path
  ""
  [str]
  ((comp #(s/replace % #"-" ".")
         #(s/replace % #"\+" "/"))
   str))

(defn resources->paths
  ""
  [paths]
  (map #(-> % java.io.File. .getAbsolutePath) paths))

(defn resources-map
  ""
  []
  (zipmap (resources-ids)
          (map resource->path (resources-ids))))

(defn build-dirs
  ""
  []
  (let [paths (map resource->path (resources-ids))
        dirs (distinct (map #(-> % fs/parent fs/base-name) paths))]
    (doseq [dir dirs]
      (fs/mkdir dir))))

(defn build-resources
  ""
  []
  (build-dirs)
  (let [resmap (resources-map)]
    (doseq [[k v] resmap]
      (base64-string->gunzip k v))))

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
       [:link {:rel "shortcut icon" :href "ico/favicon.ico" :type "image/x-icon"}]
       [:link {:rel "stylesheet" :href "css/custom.css" :media "all"}]
       [:link {:rel "stylesheet" :href "css/zenburn.css"}]
       [:script {:src "js/highlight.pack.js"}]
       [:script "hljs.initHighlightingOnLoad();"]]
      [:body {:id "page-wrap"}
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
  (build-resources)
  (let [output (or (:output opts))]
    (with-open [w (writer output)]
      (.write w (wrap opts args)))))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-opts)]
    (cond
      (:help options) (exit 0 (usage summary))
      (>= (count arguments) 1) (dump options arguments)
      :else (exit 1 (error-msg errors)))))
