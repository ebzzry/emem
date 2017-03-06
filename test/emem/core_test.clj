(ns emem.core-test
  (:require [clojure.test :refer :all]
            [emem.core :refer :all])
  (:use emem.util emem.html))

(def text-input-1
  "# foo **bar** baz")

(def text-input-2
  "Some Title\n==========\n\n## Section 1\n\nLorem ipsum dolor sit *amet*, consectetuer adipiscing elit. Donec\nodio. Quisque volutpat mattis eros. **Nullam** malesuada erat ut\nturpis. _Suspendisse_ urna nibh, viverra non, semper suscipit, posuere\na, pede.\n\n    $ foo bar baz\n    # qux quux\n\n## Section 2\n\n> Donec nec justo eget felis facilisis fermentum. Aliquam porttitor\n> mauris sit amet orci. Aenean dignissim pellentesque felis.\n\n```\nblah blah blah\n```\n\n")

(def text-expect-1a
  "<h1>foo <strong>bar</strong> baz</h1>")

(def text-expect-2a
  "<h1>Some Title</h1><h2>Section 1</h2><p>Lorem ipsum dolor sit <em>amet</em>, consectetuer adipiscing elit. Donec odio. Quisque volutpat mattis eros. <strong>Nullam</strong> malesuada erat ut turpis. <i>Suspendisse</i> urna nibh, viverra non, semper suscipit, posuere a, pede.</p><pre><code>$ foo bar baz\n# qux quux</code></pre><h2>Section 2</h2><blockquote><p> Donec nec justo eget felis facilisis fermentum. Aliquam porttitor  mauris sit amet orci. Aenean dignissim pellentesque felis. </p></blockquote><pre><code>blah blah blah\n</code></pre>")

(def text-expect-1b
  "<!DOCTYPE html>\n<html><head><title># foo **bar** baz</title><meta charset=\"utf-8\" /><meta contents=\"max-age=86400\" http-equiv=\"Cache-control\" /><meta content=\"width=device-width,initial-scale=1.0,user-scalable=yes\" name=\"viewport\" /></head><body><div id=\"content\"><h1>foo <strong>bar</strong> baz</h1></div><script src=\"static/js/highlight.pack.js\"></script><script>hljs.initHighlightingOnLoad();</script></body></html>")

(def text-expect-2b
  "<!DOCTYPE html>\n<html><head><title>Some Title</title><meta charset=\"utf-8\" /><meta contents=\"max-age=86400\" http-equiv=\"Cache-control\" /><meta content=\"width=device-width,initial-scale=1.0,user-scalable=yes\" name=\"viewport\" /></head><body><div id=\"content\"><h1>Some Title</h1><h2>Section 1</h2><p>Lorem ipsum dolor sit <em>amet</em>, consectetuer adipiscing elit. Donec odio. Quisque volutpat mattis eros. <strong>Nullam</strong> malesuada erat ut turpis. <i>Suspendisse</i> urna nibh, viverra non, semper suscipit, posuere a, pede.</p><pre><code>$ foo bar baz\n# qux quux</code></pre><h2>Section 2</h2><blockquote><p> Donec nec justo eget felis facilisis fermentum. Aliquam porttitor  mauris sit amet orci. Aenean dignissim pellentesque felis. </p></blockquote><pre><code>blah blah blah\n</code></pre></div><script src=\"static/js/highlight.pack.js\"></script><script>hljs.initHighlightingOnLoad();</script></body></html>")

(defn ftest
  [in out]
  (let [temp1 (string->temp in)
        temp2 (temp-file)]
    (convert [temp1] :out (absolute-path temp2) :plain true)
    (let [output (slurp temp2)]
      (delete temp1)
      (delete temp2)
      (is (= output out)))))

(deftest file-test-1
  (ftest text-input-1 text-expect-1b))

(deftest file-test-2
  (ftest text-input-2 text-expect-2b))

(deftest string-test-1
  (is (= (markdown text-input-1) text-expect-1a)))

(deftest string-test-2
  (is (= (markdown text-input-2) text-expect-2a)))
