__THIS FIRST VERSION IS STILL A WORK IN PROGRESS__

# Vivid Clojure Maven Plugin
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)

`clojure-maven-plugin` integrates Clojure tooling into your Maven builds.
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

### `leiningen` goal

Execute Leiningen directly within Maven's running process.
Faster and less resource-intensive than running `lein` in a sub-process. 

```xml
<plugin>
    <groupId>vivid</groupId>
    <artifactId>clojure-maven-plugin</artifactId>
    <version>0.1.0</version>
    <executions>
        <execution>
            <id>leiningen-release-build</id>         <!-- Each execution requires a unique ID -->
            <phase>compile</phase>                   <!-- Tie goal execution to the desired Maven phase -->
            <goals>
                <goal>leiningen</goal>               <!-- The vivid:clojure-maven-plugin Leiningen goal -->
            </goals>
            <configuration>

                <!-- Immediately prior to running Leiningen, vivid:clojure-maven-plugin will
                     automatically download the specified version of Leiningen and its
                     dependencies as necessary using Maven's dependency resolution system. -->
                <version>2.9.3</version>

                <!-- Leiningen tasks, aliases. Any valid 'lein' CLI arguments can be used here. -->
                <args>release</args>

            </configuration>
        </execution>
    </executions>
</plugin>
```



## Development

Run the tests and build the deliverables:

```bash
bin/test.sh
```

*Motivation*: Among the methods of integration Clojure tooling into Maven, none provided the integrative experience of IntelliJ (Maven classpath) and CI (JUnit reporting).



## License and Attributions

This project is licensed under the [Apache License Version 2.0](LICENSE.txt), modulo portions derived from these donors:

- [Inge Solvoll's `lein-maven-plugin`](https://github.com/ingesolvoll/lein-maven-plugin), MIT License.
  [Attribution](src/main/resources/licenses/LICENSE-ingesolvoll-lein-maven-plugin.txt).
- [Talios' `clojure-maven-plugin`](https://github.com/talios/clojure-maven-plugin), Eclipse Public License - v 1.0.
  [Attribution](src/main/resources/licenses/LICENSE-talios-clojure-maven-plugin.html).



## TODO

- Implementation: `clojure` goal, `clojure.test` runner, JUnit-style reporting, more integration tests.
- CI, Sonar, dist.
- clojure-toolbox.com



© Copyright Vivid Inc.
