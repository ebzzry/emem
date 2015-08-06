(ns emem.util-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [emem.util :as u])
  (:import [java.io File]
           [java.io ByteArrayInputStream File]))

(def text-input-1
  "# foo **bar** baz")

(def text-input-2
  "Some Title\n==========\n\n## Section 1\n\nLorem ipsum dolor sit *amet*, consectetuer adipiscing elit. Donec\nodio. Quisque volutpat mattis eros. **Nullam** malesuada erat ut\nturpis. _Suspendisse_ urna nibh, viverra non, semper suscipit, posuere\na, pede.\n\n    $ foo bar baz\n    # qux quux\n\n## Section 2\n\n> Donec nec justo eget felis facilisis fermentum. Aliquam porttitor\n> mauris sit amet orci. Aenean dignissim pellentesque felis.\n\n```\nblah blah blah\n```\n\n")

(defn make-temp
  []
  (let [temp (u/mktemp)
        text (u/string-input-stream "# foo\n\n## bar\n### baz\n")]
    (spit temp (slurp text))
    temp))

(deftest quo
  (is (= (u/quo (= 1 1) true) true))
  (is (= (u/quo (= 1 0) true) "")))

(deftest first-line
  (is (= "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam rutrum a"
         (u/first-line (u/find-resource "test/lorem.txt")))))

(deftest last-line
  (is (= "non, euismod maximus mauris."
         (u/last-line (u/find-resource "test/lorem.txt")))))

(deftest file
  (let [t (make-temp)
        r (class (u/file t))]
    (u/delete t)
    (is (= java.io.File r))))

(deftest pwd
  (is (= java.io.File (class (u/pwd)))))

(deftest exists?
  (is (= true (u/exists? (File. ".")))))

;; Unix only
(deftest abspath
  (is (= true (.startsWith (u/abspath (File. ".")) "/"))))

(deftest parent
  (is (= nil (u/parent (io/file "/"))))
  (is (= "/" (u/parent (io/file "/foo/"))))
  (is (= "/foo" (u/parent (io/file "/foo/bar/")))))

(deftest files-exist?
  (is (= true (u/files-exist? (map io/file ["." ".."]))))
  (is (= false (u/files-exist? (map io/file ["/foo" ".."])))))

(deftest mktemp
  (let [t (u/mktemp)
        r (u/exists? t)]
    (u/delete t)
    (is (= true r))))

(deftest tempv
  (let [[t] (u/tempv)
        r (u/exists? t)
        v (u/tempv ["foo" "bar"])]
    (u/delete t)
    (is (= true r))
    (is (= true (and (every? string? v)
                     (= (count v) 2))))))

(deftest string-input-stream
  (is (= ByteArrayInputStream
         (class (u/string-input-stream text-input-1)))))

(deftest string->temp
  (let [t (io/file (u/string->temp text-input-1))
        r (slurp t)]
    (u/delete t)
    (is (= r "# foo **bar** baz"))))

(deftest b64-encode
  (let [t (u/mktemp)]
    (u/b64-encode (u/find-resource "test/b64-in.txt") t)
    (let [r (slurp t)]
      (u/delete t)
      (is (= "Zm9vIGJhciBiYXoK" r)))))

(deftest b64-decode
  (let [t (u/mktemp)]
    (u/b64-decode (u/find-resource "test/b64-out.txt") t)
    (let [r (slurp t)]
      (u/delete t)
      (is (= "foo bar baz\n" r)))))

(deftest gunzip
  (let [t (u/mktemp)]
    (u/gunzip (u/find-resource "test/lo.txt.gz") t)
    (let [r (slurp t)]
      (u/delete t)
      (is (= "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
             r)))))

(deftest find-first
  (let [v1 [62 58 0 99 66 77]]
    (is (= 99 (u/find-first odd? v1)))))

(deftest find-resource
  (is (= java.io.BufferedInputStream
         (class (u/find-resource "etc/VERSION")))))

(deftest get-resources
  (is (= true (seq? (u/get-resources "etc"))))
  (is (= true (seq? (u/get-resources ".")))))

(deftest out
  (is (= (u/out {:out "foo"}) {:out "foo"}))
  (is (= (:out (u/out {:in "foo"})) *out*)))

(deftest modtime
  (is (= java.lang.Long (class (u/modtime ".")))))
