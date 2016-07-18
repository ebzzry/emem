(ns emem.util-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io])
  (:use emem.util)
  (:import [java.io File ByteArrayInputStream File]))

(def text-input-1
  "# foo **bar** baz")

(def text-input-2
  "Some Title\n==========\n\n## Section 1\n\nLorem ipsum dolor sit *amet*, consectetuer adipiscing elit. Donec\nodio. Quisque volutpat mattis eros. **Nullam** malesuada erat ut\nturpis. _Suspendisse_ urna nibh, viverra non, semper suscipit, posuere\na, pede.\n\n    $ foo bar baz\n    # qux quux\n\n## Section 2\n\n> Donec nec justo eget felis facilisis fermentum. Aliquam porttitor\n> mauris sit amet orci. Aenean dignissim pellentesque felis.\n\n```\nblah blah blah\n```\n\n")

(defn make-temp
  []
  (let [temp (temp-file)
        text (string-input-stream "# foo\n\n## bar\n### baz\n")]
    (spit temp (slurp text))
    temp))

(deftest quo-test
  (is (= (quo (= 1 1) true) true))
  (is (= (quo (= 1 0) true) "")))

(deftest first-line-test
  (is (= "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam rutrum a"
         (first-line (find-resource "test/lorem.txt")))))

(deftest last-line-test
  (is (= "non, euismod maximus mauris."
         (last-line (find-resource "test/lorem.txt")))))

(deftest file-test
  (let [t (make-temp)
        r (class (file t))]
    (delete t)
    (is (= java.io.File r))))

(deftest pwd-test
  (is (= java.io.File (class (pwd)))))

(deftest exists?-test
  (is (= true (exists? (File. ".")))))

;; Unix only
(deftest abspath-test
  (is (= true (.startsWith (abspath (File. ".")) "/"))))

(deftest parent-test
  (is (= nil (parent (io/file "/"))))
  (is (= "/" (parent (io/file "/foo/"))))
  (is (= "/foo" (parent (io/file "/foo/bar/")))))

(deftest files-exist?-test
  (is (= true (files-exist? (map io/file ["." ".."]))))
  (is (= false (files-exist? (map io/file ["/foo" ".."])))))

(deftest temp-file-test
  (let [t (temp-file)
        r (exists? t)]
    (delete t)
    (is (= true r))))

(deftest tempv-test
  (let [[t] (tempv)
        r (exists? t)
        v (tempv ["foo" "bar"])]
    (delete t)
    (is (= true r))
    (is (= true (and (every? string? v)
                     (= (count v) 2))))))

(deftest string-input-stream-test
  (is (= ByteArrayInputStream
         (class (string-input-stream text-input-1)))))

(deftest string->temp-test
  (let [t (io/file (string->temp text-input-1))
        r (slurp t)]
    (delete t)
    (is (= r "# foo **bar** baz"))))

(deftest b64-encode-test
  (let [t (temp-file)]
    (b64-encode (find-resource "test/b64-in.txt") t)
    (let [r (slurp t)]
      (delete t)
      (is (= "Zm9vIGJhciBiYXoK" r)))))

(deftest b64-decode-test
  (let [t (temp-file)]
    (b64-decode (find-resource "test/b64-out.txt") t)
    (let [r (slurp t)]
      (delete t)
      (is (= "foo bar baz\n" r)))))

(deftest gunzip-test
  (let [t (temp-file)]
    (gunzip (find-resource "test/lo.txt.gz") t)
    (let [r (slurp t)]
      (delete t)
      (is (= "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
             r)))))

(deftest find-first-test
  (let [v1 [62 58 0 99 66 77]]
    (is (= 99 (find-first odd? v1)))))

(deftest find-resource-test
  (is (= java.io.BufferedInputStream
         (class (find-resource "etc/VERSION")))))

(deftest get-resources-test
  (is (= true (seq? (get-resources "etc"))))
  (is (= true (seq? (get-resources ".")))))

(deftest mod-time-test
  (is (= java.lang.Long (class (mod-time ".")))))
