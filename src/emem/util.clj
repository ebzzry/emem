(ns emem.util
  (:require [clojure.java.io :as io]
            [clojure.data.codec.base64 :as b64])
  (:import [java.util.zip GZIPInputStream]
           [java.io ByteArrayInputStream File])
  (:gen-class))

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

(defn id
  "Returns identity of ARG."
  [arg]
  (identity arg))

(defn ex
  "Evaluates F, then exits to OS with CODE."
  [f & [code]]
  (f)
  (System/exit (or code 0)))

(defn msg
  "Displays TEXT if OVERRIDE >= LEVEL. OVERRIDE and LEVEL defaults
to 0 and 1, respectively."
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
  (ex #(println msg) text))

(defn restream
  "Returns RES as BufferedInputStream"
  [res]
  (-> res io/resource io/input-stream))

(defn file
  "Returns a File if PATH exists, false otherwise."
  [path]
  (io/file path))

(defn pwd
  "Returns the current directory."
  []
  (-> "." io/file .getCanonicalFile))

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

(defn abspath
  "Returns absolute path"
  [path]
  (meth getAbsolutePath (file path)))

(defn parent
  "Returns parent path"
  [path]
  (meth getParent (file path)))

(defn files-ok?
  "Returns true if all FILES exist."
  [paths]
  (every? #(exists? %) paths))

(defn mktemp
  "Returns path to a new temp file."
  []
  (File/createTempFile "tmp" ""))

(defn- temp
  "Creates a temp file, then returns its absolute path."
  []
  (abspath (mktemp)))

(defn tempv
  "Creates a temp file and returns a vector with its
absolute path, if ARGS is empty; otherwise, returns ARGS"
  ([]
   [(temp)])
  ([args]
   (if (empty? args) [(temp)] args)))

(defn string-input-stream
  "Returns a ByteArrayInputStream for the given String."
  ([^String s]
     (ByteArrayInputStream. (.getBytes s)))
  ([^String s encoding]
     (ByteArrayInputStream. (.getBytes s encoding))))

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
    (spit output (slurp in))
    (io/delete-file input)))

(defn gunzip-b64
  "Consumes a base64 string INPUT, decompresses using GZIP,
then writes to OUTPUT."
  [input output]
  (gunzip (b64-decode-temp input) output))
