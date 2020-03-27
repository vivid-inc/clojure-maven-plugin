# Vivid Clojure Maven Plugin

`clojure-maven-plugin` is a single-minded Maven plugin that runs your Clojure commands and integrates your Clojure `clojure.test`-based tests into your Maven build.
It is designed to cooperate with critters commonly found in Clojure- and Java-slinging development shops, likely the ones you are using now:

- Apache Maven, which you have entrusted to be the primary driver of your build.
- The `clojure` CLI command and its `deps.edn` system.
- Optional `clojure.test` execution and build breaking.
- Optional JUnit-style test reporting output, compatible with virtually all Java-savvy Continuous Integration systems.

Intentionally Spartan: If you note a feature that doesn't directly contribute to running your Clojure commands and Clojure-based tests from Maven, let us know so that we may excise it.



## Usage

Include Clojure code into a Maven project that is not of type `clojure`.
So, you have some Clojure code or test code that needs access to the classpath assembled by Maven; this plugin does just that.
Structure your Clojure code or `clojure.test` code around [`deps.edn`](https://clojure.org/reference/deps_and_cli).
Add the Maven plugin to your POM.
Set the goal in the POM.
`clojure` needs to be on the path, or alternatively specify a path using `clojureExecutable`.
Optionally provide args.
Bind that to a Maven phase.
Setup Clojars as a Maven repo.
Add a dependency to Clojure in your POM, or deps.edn.



## Development

Run the tests and build the deliverables:

```bash
bin/test.sh
```

*Motivation*: Among the methods of integration Clojure tooling into Maven, none provided the integrative experience of IntelliJ (Maven classpath) and CI (JUnit reporting).



## License and Attributions

This project is dual-licensed under the Apache Public License and the Eclipse Public License, modulo portions derived from these donors:

- [Talios clojure-maven-plugin](https://github.com/talios/clojure-maven-plugin), EPL 1.0 license
(TODO Link directly to the hash 8ce0d7dab93a418cfba0bcf68943c31291bcdc23)



## TODO

- Licensing: Apache and EPL dual license
- CI, Sonar, dist
- Implementation



© Copyright Vivid Inc.
