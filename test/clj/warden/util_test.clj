(ns warden.util-test
  (:require [clojure.test :refer [deftest is]]
            [warden.util :as util]))

(deftest pretty
  (is (= "{:foo \"bar\", :baz \"boo\"}\n"
         (util/pretty {:foo "bar"
                       :baz "boo"}))))

(deftest uuid
  (is (string? (util/uuid))))

(deftest map-vals
  (is (= {:a 2 :b 3 :c 4}
         (util/map-vals inc {:a 1 :b 2 :c 3}))))

(deftest unkeyword
  (is (= "foo" (util/unkeyword "foo")))
  (is (= "foo" (util/unkeyword :foo)))
  (is (= "foo/bar" (util/unkeyword :foo/bar))))
