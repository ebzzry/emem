(ns emem.core-test
  (:require [clojure.test :refer :all]
            [emem.core :refer :all]))

;; example-based testing
(deftest foo-test
  (testing "foo"
    (is (= 0 0))))

(deftest bar-test
  (testing "bar"
    (are [x y] (= 5 (+ x y))
         2 3
         1 4
         3 2)))

;; property-based testing
;; (defspec edn-roundtrips 50
;;   (prop/for-all [a gen/any]
;;     (= a (-> a prn-str edn/read-string))))
