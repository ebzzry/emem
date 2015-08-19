(ns emem.util
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.data.codec.base64 :as b64]
            [cpath-clj.core :as cp])
  (:import [java.util.zip GZIPInputStream GZIPOutputStream]
           [java.io File ByteArrayInputStream ByteArrayOutputStream]))

(defn quo
  "Returns the empty string if TEST evaluates to false; otherwise
  returns THEN."
  [test then]
  (if test then ""))

(defn first-line
  "Returns the first line of a file."
  [path]
  (with-open [file (io/reader path)]
    (str (first (line-seq file)))))

(defn last-line
  "Returns the first line of a file."
  [path]
  (with-open [file (io/reader path)]
    (str (last (line-seq file)))))

(defn exit
  "Evaluates F, then exits to OS with CODE."
  ([]
   (exit #() 0))
  ([f]
   (exit f 0))
  ([f code]
   (f)
   (System/exit code)))

(defn msg
  "Displays TEXT if OVERRIDE >= LEVEL. OVERRIDE and LEVEL defaults to
  0 and 1, respectively."
  ([text]
   (msg text 1 0))
  ([text level]
   (msg text level 0))
  ([text level override]
   (when (>= override level)
     (println text))))

(defn bye
  "Displays TEXT to *out*, and exits the program with status CODE."
  [text code]
  (exit #(println msg) text))

(defn file
  "Returns a File if PATH exists, false otherwise."
  [path]
  (io/file path))

(defn pwd
  "Returns the current directory."
  []
  (-> "." file .getCanonicalFile))

(defmacro ^:private meth
  "Returns Java METHOD on ARG if ARG is true, false otherwise."
  [method arg]
  `(if ~arg (. ~arg ~method) false))

(defn exists?
  "Returns true if PATH exists."
  [path]
  (meth exists (file path)))

(defn delete
  "Deletes file."
  [path]
  (meth delete (file path)))

(defn root
  "Returns root name of PATH."
  [path]
  (let [name (.getName (file path))]
    (let [dot (.lastIndexOf name ".")]
      (if (pos? dot)
        (subs name 0 dot)
        name))))

(defn abspath
  "Returns absolute path of PATH."
  [path]
  (meth getAbsolutePath (file path)))

(defn parent
  "Returns parent path of PATH."
  [path]
  (meth getParent (file path)))

(defn dir?
  "Returns true if PATH is a directory."
  [path]
  (meth isDirectory (file path)))

(defn file?
  "Returns true if PATH is a File object."
  [path]
  (meth isFile (file path)))

(defn parent*
  "Returns parent path of PATH, if PATH is a regular file;
  otherwise, return PATH."
  [path]
  (if (dir? path)
    path
    (-> path file abspath file parent file)))

(defn files-exist?
  "Returns true if all FILES exist."
  [files]
  (every? exists? files))

(defn mktemp
  "Returns path to a new temp file."
  []
  (File/createTempFile "tmp" ""))

(defn tempv
  "Creates a temp file and returns a vector with its absolute path, if
  ARGS is empty; otherwise, returns ARGS"
  ([]
   [(abspath (mktemp))])
  ([args]
   (if (empty? args) [(abspath (mktemp))] args)))


(defn string-input-stream
  "Returns a ByteArrayInputStream for the given String."
  ([^String s]
     (ByteArrayInputStream. (.getBytes s)))
  ([^String s encoding]
     (ByteArrayInputStream. (.getBytes s encoding))))

(defn string-output-stream
  "Returns a ByteArrayOutputStream for the given String."
  []
  (ByteArrayOutputStream.))

(defn string->temp
  "Returns path to temp file to contain STR."
  [str]
  (let [temp (abspath (mktemp))
        input (string-input-stream str)]
    (spit (file temp) (slurp input))
    temp))

(defn b64-encode
  "Base64-encode the file in SRC; output to DEST."
  [^String src ^String dest]
  (with-open [in (io/input-stream src)
              out (io/output-stream dest)]
    (b64/encoding-transfer in out)))

(defn b64-decode
  "Base64-decode the file in SRC; output to DEST."
  [src dest]
  (with-open [in (io/input-stream src)
              out (io/output-stream dest)]
    (b64/decoding-transfer in out)))

(defn b64-decode-temp
  "Decodes a base64 string to a temporary file."
  [src]
  (let [temp (mktemp)]
    (b64-decode (string-input-stream src) temp)
    (abspath temp)))

(defn gunzip
  "Decompresses INPUT with GZIP, then writes to OUTPUT."
  [input output]
  (with-open [in (-> input io/input-stream GZIPInputStream.)
              out (io/output-stream output)]
    (spit output (slurp in))))

(defn gunzip-b64
  "Consumes a base64 string INPUT, decompresses using GZIP,
  then writes to OUTPUT."
  [input output]
  (gunzip (b64-decode-temp input) output))

(defn re-stream
  "Returns RES as BufferedInputStream"
  [res]
  (-> res io/resource io/input-stream))


(defn find-first
  "Returns first item from sequence that satisfiers F"
  [f seq]
  (first (filter f seq)))

(defn resources
  "Returns a PATH:URI map of the resources under PATH."
  [path]
  (cp/resources (io/resource path)))

(defn find-resource
  "Returns a a relative path in the resources that matches PATH."
  [path]
  (let [[base & res] (s/split path #"/")
        file (str "/" (s/join "/" res))
        resk (keys (resources base))]
    (when-let [match (find-first #(= file %) resk)]
      (re-stream (str base match)))))

(defn get-resources
  "Returns a list of resources under PATH."
  [path]
  (map #(subs % 1) (keys (resources path))))

(defn list-resources
  "Displays all the resources under PATH."
  [path]
  (doseq [res (get-resources path)]
    (println res)))

(defn merge-out
  "Returns MAP if KEY is found in MAP. Otherwise, merge MAP with a
  map, wherein the value of KEY is DEFAULT. If DEFAULT is nil, the
  value of KEY is *out*."
  [key map default]
  (if (key map)
    map
    (merge map {key (or default *out*)})))

(defn merge-options
  "Returns a map, applying the key :out to OPTS, setting the value to
  OUT. The two-arity version specifies the value of :out. The
  sigle-arity version lets the user set the value of :out. If no value
  was set, it defaults to *out."
  ([opts]
   (merge-options opts nil))
  ([opts out]
   (merge-out :out opts out)))

(defn merge-true
  "Returns a map where the value of KEY is true, merged with with
  MAP."
  [map key]
  (merge map {key true}))

(defn out
  "Returns *out* if \"-\" is present in OPTS, on key :out. Otherwise,
  return value of :out of OPTS."
  [opts]
  (let [out (:out opts)]
    (cond
      (= out "-") *out*
      :else out)))

(defn mod-time
  "Returns the last modified time of PATH."
  [path]
  (-> path file .lastModified))

(defn mod-times
  "Returns the last modified times of PATHS."
  [paths]
  (map mod-time paths))

(defn base-name
  "Returns name of PATH, sans directory."
  [path]
  (-> path file .getName))

(defn split-name
  "Returns [name extension] of PATH"
  [path]
  (let [name (base-name path)
        index (.lastIndexOf name ".")]
    (if (pos? index)
      [(subs name 0 index) (subs name index)]
      [name nil])))

(defn file-name
  "Returns name of PATH, sans extension."
  [path]
  (first (split-name path)))

(defn file-extension
  "Returns the extension name of PATH."
  [path]
  (last (split-name path)))

(defn abs-base-name
  "Returns absolute name of PATH, sans directory."
  [path]
  (-> path file .getAbsolutePath))

(defn abs-parent
  "Returns absolute parent of PATH. If PATH is a directory, return
  PATH."
  [path]
  (if (dir? path)
    path
    (parent (abs-base-name path))))

(defn common-directory?
  "Returns true if ARGS have the same parent directory."
  [args]
  (reduce = (map abs-parent args)))

(defn abs-split-name
  "Returns abs [name path] of PATH"
  [path]
  (let [name (abs-base-name path)
        index (.lastIndexOf name ".")]
    (if (pos? index)
      [(subs name 0 index) (subs name index)]
      [name nil])))

(defn abs-file-name
  "Returns absolute name of PATH, sans extension."
  [path]
  (first (abs-split-name path)))

(defn list-objects
  "List directory entries in PATH."
  [path]
  (seq (-> path file .listFiles)))

(defn list-names
  "List directory entries in PATH, in human form."
  ([]
   (list-names "."))
  ([path]
   (map abspath (list-objects path))))

(defn list-names-ext
  "Returns the names of files with file extension EXTENSION."
  [path extension]
  (filter #(= extension (file-extension %)) (list-names path)))

(defn in?
  "Returns true if *in* is present in ARGS."
  [args]
  (some #{*in*} args))

(defn out?
  "Returns true if *out* is present in ARGS."
  [args]
  (some #{*out*} args))
