(ns non-passing-test
  "Exercises each kind of non-passing test."
  (:require [clojure.test :refer :all]))

(deftest false-assertion
         (is (= :yes :no)))

(deftest cause-arithmetic-exception
         (/ 1 0))

(deftest no-exception-thrown
  (is (thrown? ArithmeticException (+ 0 1))))

(deftest documented
  (is (= 5 (+ 2 2)) "Ill-considered arithmetic"))

(deftest multiple-failing-assertions-with-template-expression
         (are [x y] (= x y)
              0 (+ 1 1)
              0 (* 2 3)))
