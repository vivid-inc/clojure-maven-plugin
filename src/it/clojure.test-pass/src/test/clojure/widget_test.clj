(ns widget-test
  "Exercises the Java class sample.Widget, whose .class file is available via the classpath in target/test-classes"
  (:require [clojure.test :refer :all])
  (:import (sample Widget)))

(deftest widget-add
  (is (= 7 (Widget/add 3 4))))
