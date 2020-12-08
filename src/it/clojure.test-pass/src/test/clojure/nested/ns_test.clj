(ns nested.ns-test
  (:require
    [clojure.test :refer [deftest is testing]]))

(deftest passing-test
  (testing "I pass"
    (is (= true true))))
