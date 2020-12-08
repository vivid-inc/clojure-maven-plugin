![Vivid Clojure Maven Plugin][1]

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)
[![Current version](https://img.shields.io/clojars/v/vivid/clojure-maven-plugin.svg?color=blue&style=flat-square)](https://clojars.org/vivid/clojure-maven-plugin)
[![CircleCI build status](https://circleci.com/gh/vivid-inc/clojure-maven-plugin/tree/release-0.3.0.svg)](https://circleci.com/gh/vivid-inc/clojure-maven-plugin)
[![SonarCloud](https://sonarcloud.io/api/project_badges/measure?project=vivid-inc_clojure-maven-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=vivid-inc_clojure-maven-plugin)


`clojure-maven-plugin` conveniently integrates Clojure tooling and processing steps into your Maven builds.
Intentionally Spartan, it is specifically designed to wrangle those critters commonly found in Clojure- and Java-slinging development shops, likely the ones you are using now:

- Apache Maven, the primary driver of your build.
- `clojure` CLI tooling and its `deps.edn` ecosystem.
- Leiningen `project.clj` workflows.

This plugin doesn't offer any new packaging type such as `clojure`.



## Usage

First, ensure your Maven build uses the Clojars repository for dependency and plugin resolution with this snippet:
```xml
<repository>
    <id>clojars.org</id>
    <url>https://repo.clojars.org/</url>
</repository>
```
in each of the `<repositories>` and `<pluginRepositories>` sections in the appropriate Maven
configuration, such as your `pom.xml`.

### `clojure` goal

Execute the CLI `clojure` command in a sub-process using the Maven project classpath.

```xml
<plugin>
    <groupId>vivid</groupId>
    <artifactId>clojure-maven-plugin</artifactId>
    <version>0.3.0</version>
    <executions>
        <execution>
            <id>leiningen-release-build</id>         <!-- Each execution requires a unique ID -->
            <phase>compile</phase>                   <!-- Tie goal execution to the desired Maven phase -->
            <goals>
                <goal>clojure</goal>                 <!-- The vivid:clojure-maven-plugin Clojure goal -->
            </goals>
            <configuration>

                <!-- Optional path to the clojure executable -->
                <executable>clojure</executable>

                <!-- Optional arguments to Clojure. CDATA might be necessary to handle dashes. -->
                <args>release</args>

                <!-- Maven classpath scope provided to `clojure`.
                     Defaults to COMPILE. Other values are TEST and NONE. -->
                <classpathScope>TEST</classpathScope>

                <!-- Defaults to Maven's default of just src/main/clojure -->
                <sourcePaths>
                    <sourcePath>src/main/clojure</sourcePath>
                </sourcePaths>

                <!-- Defaults to Maven's default of just src/test/clojure -->
                <testPaths>
                    <testPath>src/test/clojure</testPath>
                </testPaths>

            </configuration>
        </execution>
    </executions>
</plugin>
```



### `clojure.test` goal

__Skip tests__ by setting the `skip` parameter to true.
Also honors the `maven.test.skip` parameter and `-Dclojure-maven-plugin.clojure.test.skip=true`.




### `deps.edn` goal

Writes a [`deps.edn`](https://clojure.org/reference/deps_and_cli) file that replicates each of the `clojure` goal execution configurations in the POM.
They can then be directly run by the `clojure` CLI tools.
The Maven goal runs during the `generate-resources` phase by default.
```xml
<plugin>
    <groupId>vivid</groupId>
    <artifactId>clojure-maven-plugin</artifactId>
    <version>0.3.0</version>
    <executions>
        <execution>
            <id>hello-wumpus</id>                     <!-- Each 'clojure' goal execution ID servers as the deps.edn alias -->
            <goals><goal>clojure</goal></goals>
            ...
        </execution>
        ...
        <execution>
            <goals>
                <goal>deps.edn</goal>                 <!-- The vivid:clojure-maven-plugin deps.edn goal -->
            </goals>
            <configuration>

                <!-- Optional. Specify where to write the file. If a directory, deps.edn will
                     be written to that directory. Paths in deps.edn are written relative to
                     Maven's $project.basedir -->
                <pathname>../projects/deps.edn</pathname>

            </configuration>
        </execution>
    </executions>
</plugin>
```
To run the `deps.edn` goal from the CLI:
```bash
$ mvn vivid:clojure-maven-plugin:deps.edn
...
[INFO] --- clojure-maven-plugin:0.3.0:deps.edn (default-cli) @ multiple-use-project ---
[INFO] Wrote deps.edn
```
Continuing with the running example, the `deps.edn` file now has an `:alias` for `:hello-wumpus` that replicates the same classpath and options as its originating `clojure` Maven goal:
```edn
{:aliases {:hello-wumpus {:extra-paths ["src/main/clojure"
                                        "target/classes"]
                          :main-opts ["-m hello-wumpus.core"]
                          :extra-deps {org.clojure/clojure {:mvn/version "1.10.1"}}}
           ... }}
```
and can be run with:
```bash
$ clojure -A:hello-wumpus
Hello from planet Irata!
```
Useful for bringing a build into other tooling for further work or experimentation.
Now, wasn't that .. anticlimactic? And, boring? And time-saving? And effective? And reliable? Just like Maven..



### `leiningen` goal

Execute Leiningen directly within Maven's running process.
Faster and less resource-intensive than running `lein` in a sub-process.

```xml
<plugin>
    <groupId>vivid</groupId>
    <artifactId>clojure-maven-plugin</artifactId>
    <version>0.3.0</version>
    <executions>
        <execution>
            <id>leiningen-release-build</id>         <!-- Each execution requires a unique ID -->
            <phase>compile</phase>                   <!-- Bind goal execution to the desired Maven phase -->
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

_Current working directory_:
For the duration of an invocation of Leiningen, both `user.dir` and Leiningen's own `leiningen.core.main/*cwd*` var are
set to the Maven module's `basedir`, giving Leiningen and its various tasks the information they need to correctly
determine the current working directory.
Afterwards `user.dir` is restored to its prior value.
Portions of a Leiningen run might express ill behavior with file paths in the context of a Maven multi-module build;
tasks must account for `user.dir`.


## Development

Run the tests and build the deliverables:

```bash
bin/test.sh
```

*Motivation*: Among the methods of integration Clojure tooling into Maven, none provided the integrative experience of IntelliJ (Maven classpath) and CI (JUnit reporting).


## License and Attributions

This project is licensed under the [Apache License Version 2.0](LICENSE.txt), modulo portions derived from these donors:

- Inge Solvoll [`lein-maven-plugin`](https://github.com/ingesolvoll/lein-maven-plugin).
  [MIT License](https://raw.githubusercontent.com/ingesolvoll/lein-maven-plugin/d86e5ded9c75018b26c99ff041f3dad7079d3d1a/LICENSE).
- James Reeves [`eftest`](https://github.com/weavejester/eftest).
  [Eclipse Public License 1.0](https://raw.githubusercontent.com/weavejester/eftest/43a41b28e5981382a93503be27bce463e1b6ad4f/README.md).
- Talios [`clojure-maven-plugin`](https://github.com/talios/clojure-maven-plugin).
  [Eclipse Public License 1.0](https://raw.githubusercontent.com/talios/clojure-maven-plugin/8ce0d7dab93a418cfba0bcf68943c31291bcdc23/epl-v10.html).

© Copyright Vivid Inc.

[1]:https://github.com/vivid-inc/clojure-maven-plugin/blob/release-0.3.0/assets/artwork/vivid-clojure-maven-plugin-logo.svg?raw=true
