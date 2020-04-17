(ns failing-test
  "A test that intentionally fails."
  (:require [clojure.test :refer :all]))

(deftest failing-assertion
         (is (= :yes :no)))
