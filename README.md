  # Vivid Clojure Maven Plugin
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)
[![Current version](https://img.shields.io/clojars/v/vivid/clojure-maven-plugin.svg?color=blue&style=flat-square)](https://clojars.org/vivid/clojure-maven-plugin)
[![CircleCI build status](https://circleci.com/gh/vivid-inc/clojure-maven-plugin/tree/release-0.1.0.svg)](https://circleci.com/gh/vivid-inc/clojure-maven-plugin)
[![SonarCloud](https://sonarcloud.io/api/project_badges/measure?project=vivid-inc_clojure-maven-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=vivid-inc_clojure-maven-plugin)


`clojure-maven-plugin` integrates Clojure tooling into your Maven builds.
Intentionally Spartan, it is specifically designed to wrangle those critters commonly found in Clojure- and Java-slinging development shops, likely the ones you are using now:

- Apache Maven, the primary driver of your build.
- Leiningen `project.clj` workflows in-process in Maven.



## Usage

First, ensure your Maven build targets Clojars for dependency and plugin resolution by adding this snippet:

```xml
<repository>
    <id>clojars.org</id>
    <url>https://repo.clojars.org/</url>
</repository>
```

to each of the `<repositories>` and `<pluginRepositories>` sections in your `pom.xml`.



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



© Copyright Vivid Inc.
