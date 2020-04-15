(ns clojure-test-api-sorezore-test
  "Exercises much of the clojure.test API. All tests pass."
  (:require [clojure.test :refer :all]))

(deftest equality
  (is (= 1 1))
  (is (= :a :a))
  (is (= 'b 'b))
  (is (= "cde" "cde")))

(deftest orderings
  (testing "Smoke-test the less-than function"
    (is (< 1 2 3))))

(deftest grouped-assertions
  (testing "Outer walls"
    (testing "Mezzanine"
      (testing "Inner courtyard"
        (is (= 'pear-garden 'pear-garden))
        (testing "Arithmetic"
          (testing "with positive integers"
            (is (= 4 (+ 2 2)))
            (is (= 7 (+ 3 4))))
          (testing "with negative integers"
            (is (= -4 (+ -2 -2)))
            (is (= -1 (+ 3 -4)))))))))

(deftest documented
         (is (= 4 (+ 2 2)) "Well-considered arithmetic"))

(deftest exceptions
         (is (thrown? ArithmeticException (/ 1 0)))
         (is (thrown-with-msg? ArithmeticException #"Divide by zero"
                               (/ 1 0))))

(with-test
  (defn my-function [x y]
        (+ x y))
  (is (= 4 (my-function 2 2)))
  (is (= 7 (my-function 3 4))))

(deftest addition
         (is (= 4 (+ 2 2)))
         (is (= 7 (+ 3 4))))
(deftest subtraction
         (is (= 1 (- 4 3)))
         (is (= 3 (- 7 4))))
(deftest arithmetic
         (addition)
         (subtraction))

(deftest multiple-assertions-with-template-expression
         (are [x y] (= x y)
              2 (+ 1 1)
              4 (* 2 2)))
