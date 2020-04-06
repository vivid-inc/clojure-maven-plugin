## TODO

Note the following in the README:

- The `clojure` CLI command and its `deps.edn` system.
- Optional `clojure.test` execution and Maven build breaking behavior.
- Optional JUnit-style test reporting output, compatible with virtually all Java-savvy Continuous Integration systems.

### `clojure` goal
Include Clojure code into a Maven project that is not of type `clojure`.
So, you have some Clojure code or test code that needs access to the classpath assembled by Maven; this plugin does just that.
Structure your Clojure code or `clojure.test` code around [`deps.edn`](https://clojure.org/reference/deps_and_cli).
Add the Maven plugin to your POM.
Set the goal in the POM.
`clojure` needs to be on the path, or alternatively specify a path using `clojureExecutable`.
Optionally provide args.
Bind that to a Maven phase.
Setup Clojars as a Maven repo and pluginRepo.
Add a dependency to Clojure in your POM, or deps.edn.

// TODO 'clojure.test & junit'

// Referencing
// https://github.com/cognitect-labs/test-runner/blob/master/deps.edn
// https://oli.me.uk/clojure-and-clojurescript-testing-with-the-clojure-cli/
// https://github.com/ingesolvoll/lein-maven-plugin
// https://github.com/redbadger/test-report-junit-xml

// Run the clojure.test tests.
// Write JUnit report files.
// Report results on console and back to Maven ala surefire.

### Implementation

`clojure` goal, `clojure.test` runner, JUnit-style reporting. `deps.edn` goal. More integration tests.
