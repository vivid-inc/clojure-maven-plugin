(ns failing-test
  "A test that intentionally fails."
  (:require
    [clojure.test :refer [deftest is]]))

(deftest failing-assertion
         (is (= :yes :no)))
