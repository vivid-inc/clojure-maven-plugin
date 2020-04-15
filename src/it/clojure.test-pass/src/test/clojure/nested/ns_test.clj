(ns nested.ns-test
  (:require [clojure.test :refer :all]))

(deftest passing-test
  (testing "I pass"
    (is (= true true))))

#_(deftest failing-tests
  (testing "I fail"
    (is (= 0 1)))
  (is (= {:a 1 :b 2} {:b 2 :a 1}))
  (is (= {:bar 2 :foo 1} {:bar 3 :foo 1})))



#_(deftest throwing-test
  (testing "I throw"
    (throw (Exception. "kaboom"))))
