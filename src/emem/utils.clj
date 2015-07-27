(ns emem.utils
  (:require [clojure.java.io :as io]
            [clojure.data.codec.base64 :as b64])
  (:import [java.util.zip GZIPInputStream]
           [java.io ByteArrayInputStream])
  (:gen-class))

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
  "Creates a temporary file in /tmp."
  []
  (java.io.File/createTempFile "/tmp" ""))

(defn base64-decode-temp
  "Decodes a base64 string to a temporary file."
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
    (io/delete-file input)))

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
