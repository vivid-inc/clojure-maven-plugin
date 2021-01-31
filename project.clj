(defproject vivid.clojure-maven-plugin "0.3.0"

  ; NOTE: This Leiningen project.clj is inferior to the Maven pom.xml.
  ; It exists for these purposes:
  ; - To help your IDE load the project more completely.
  ; - Afford various checks at the CLI, such as "lein ancient".

  :aliases {"qa-check" ["do"
                        ["version"]]
            ; NOTE: clj-kondo fails, because it tries to compile the Java code without its dependencies.
            "clj-kondo" ["with-profile" "clj-kondo" "run" "-m" "clj-kondo.main" "--" "--lint" "src/main/clojure"]}
  :dependencies [[eftest/eftest "0.5.9" :scope "provided"]
                 [leiningen/leiningen "2.9.3" :scope "provided"]
                 [org.clojure/clojure "1.10.1" :scope "provided"]]
  :exclusions [org.clojure/clojure]
  :java-source-paths ["src/main/java"]
  :javac-options ["-target" "1.8"]
  :manifest {"Built-By" "vivid"}
  :plugins [[lein-ancient "0.6.15"]
            [lein-cljfmt "0.7.0"]
            [lein-ns-dep-graph "0.2.0-SNAPSHOT" :exclusions [org.clojure/clojure]]
            [lein-nvd "1.1.0" :exclusions [org.slf4j/jcl-over-slf4j]]]
  :profiles {:clj-kondo {:dependencies [[org.clojure/clojure "1.9.0"]
                                        [clj-kondo "RELEASE"]]}}
  :scm {:name "git" :url "https://github.com/vivid-inc/clojure-maven-plugin"}
  :source-paths ["src/main/clojure"]
  :target-path "target/%s"

  ; These settings enable stricter checks, and are disabled in VCS:
  ;:global-vars {*warn-on-reflection* true}
  ;:pedantic? :abort
  )
