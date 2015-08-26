(ns emem.core-test
  (:require [clojure.test :refer :all]
            [emem.util :as u]
            [emem.core :refer :all]))

(def text-input-1
  "# foo **bar** baz")

(def text-input-2
  "Some Title\n==========\n\n## Section 1\n\nLorem ipsum dolor sit *amet*, consectetuer adipiscing elit. Donec\nodio. Quisque volutpat mattis eros. **Nullam** malesuada erat ut\nturpis. _Suspendisse_ urna nibh, viverra non, semper suscipit, posuere\na, pede.\n\n    $ foo bar baz\n    # qux quux\n\n## Section 2\n\n> Donec nec justo eget felis facilisis fermentum. Aliquam porttitor\n> mauris sit amet orci. Aenean dignissim pellentesque felis.\n\n```\nblah blah blah\n```\n\n")

(def text-expect-1a
  "<h1>foo <strong>bar</strong> baz</h1>")

(def text-expect-2a
  "<h1>Some Title</h1><h2>Section 1</h2><p>Lorem ipsum dolor sit <em>amet</em>, consectetuer adipiscing elit. Donec odio. Quisque volutpat mattis eros. <strong>Nullam</strong> malesuada erat ut turpis. <i>Suspendisse</i> urna nibh, viverra non, semper suscipit, posuere a, pede.</p><pre><code>$ foo bar baz\n# qux quux\n</code></pre><h2>Section 2</h2><blockquote><p> Donec nec justo eget felis facilisis fermentum. Aliquam porttitor  mauris sit amet orci. Aenean dignissim pellentesque felis. </p></blockquote><pre><code>blah blah blah\n</code></pre>")

(def text-expect-1b
  "<html><head><title># foo **bar** baz</title><meta charset=\"utf-8\" /><meta content=\"width=device-width, initial-scale=1.0, user-scalable=yes\" name=\"viewport\" /></head><body><div id=\"content\"><h1>foo <strong>bar</strong> baz</h1></div></body></html>")

(def text-expect-2b
  "<html><head><title>Some Title</title><meta charset=\"utf-8\" /><meta content=\"width=device-width, initial-scale=1.0, user-scalable=yes\" name=\"viewport\" /></head><body><div id=\"content\"><h1>Some Title</h1><h2>Section 1</h2><p>Lorem ipsum dolor sit <em>amet</em>, consectetuer adipiscing elit. Donec odio. Quisque volutpat mattis eros. <strong>Nullam</strong> malesuada erat ut turpis. <i>Suspendisse</i> urna nibh, viverra non, semper suscipit, posuere a, pede.</p><pre><code>$ foo bar baz\n# qux quux\n</code></pre><h2>Section 2</h2><blockquote><p> Donec nec justo eget felis facilisis fermentum. Aliquam porttitor  mauris sit amet orci. Aenean dignissim pellentesque felis. </p></blockquote><pre><code>blah blah blah\n</code></pre></div></body></html>")

(defn ftest
  [in out]
  (let [temp1 (u/string->temp in)
        temp2 (u/mktemp)]
    (convert [temp1] :out (u/abspath temp2) :plain true)
    (let [output (slurp temp2)]
      (u/delete temp1)
      (u/delete temp2)
      (is (= output out)))))

(deftest file-test-1
  (ftest text-input-1 text-expect-1b))

(deftest file-test-2
  (ftest text-input-2 text-expect-2b))

(deftest string-test-1
  (is (= (markdown text-input-1) text-expect-1a)))

(deftest string-test-2
  (is (= (markdown text-input-2) text-expect-2a)))
