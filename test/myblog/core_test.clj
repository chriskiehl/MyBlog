(ns myblog.core-test
  (:require [clojure.test :refer :all]
            [myblog.core :refer :all]))


(deftest another-one
  (testing "Howdy!"
    (is (= 1 1))))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))
