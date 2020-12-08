# Design notes

* The context within which these Maven goals are configured is Maven, wherein
  identifiers use the CamelCase convention prevalent in Java and Maven
  rather than the lisp-case convention natural to Clojure.


## Comparison of in-process and sub process execution

In-process:
#####PROS:
- Faster; less time & memory resource consumption
- Direct handling of return value; no conversion necessary
#####CONS:
- Clojure version cannot be specified
- Potential to bring down Maven as a whole
- Maven's classpath leaks into Clojure execution environment

Sub-process:
#####PROS:
- Full control over classpath
- Replicable at the CLI
#####CONS:
- Slow; more resource intensive
- Indirect handling of return value
