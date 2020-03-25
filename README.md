# Vivid Clojure Maven Plugin

`clojure-maven-plugin` is a singular-minded Maven plugin that runs your Clojure commands and integrates your Clojure `clojure.test`-based tests into your Maven build.
It is designed to integrate well with critters commonly found in Clojure- and Java-slinging development shops, likely the ones you are using now:

- Apache Maven, which you have entrusted to be the primary driver of your build.
- The `clojure` CLI command and its `deps.edn` system.
- Optional `clojure.test` execution and build breaking.
- Optional JUnit-style test reporting output, compatible with virtually all Java-savvy Continuous Integration systems.

Intentionally Spartan: If you note a feature that doesn't directly contribute to running your Clojure commands and Clojure-based tests from Maven, let us know so that we may excise it.



## Development

Run the tests and build the deliverables:

```bash
bin/test.sh
```

*Motivation*: Among the methods of integration Clojure tooling into Maven, none provided the integrative experience of IntelliJ (Maven classpath) and CI (JUnit reporting).


## License and Attributions

Portions of the implementation is based on code from these donors:

- [Talios clojure-magen-plugin](https://github.com/talios/clojure-maven-plugin), EPL 1.0 license

## TODO

- Licensing: Apache and EPL dual license, attributions to donor code
- CI, Sonar, dist
- Implementation



© Copyright Vivid Inc.
