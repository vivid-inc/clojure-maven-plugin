(defproject vivid.clojure-maven-plugin "0.3.0"

  ; NOTE: This Leiningen project.clj is inferior to the Maven pom.xml.
  ; It exists for these purposes:
  ; - To help your IDE load the project more completely.
  ; - Afford various checks at the CLI, such as "lein ancient".

  :aliases {"qa-check" ["do" "version," "kibit"]}
  :dependencies [[eftest/eftest "0.5.9" :scope "provided"]
                 [leiningen/leiningen "2.9.3" :scope "provided"]
                 [org.clojure/clojure "1.10.1" :scope "provided"]]
  :exclusions [org.clojure/clojure]
  :java-source-paths ["src/main/java"]
  :javac-options ["-target" "1.8"]
  :plugins
  ; Note: eastwood, yagni fail due to the Java code's dependencies being out of reach to them.
  [[lein-ancient "0.6.15"]
   [lein-kibit "0.1.6"]
   [lein-nvd "1.1.0" :exclusions [org.slf4j/jcl-over-slf4j]]]
  :source-paths ["src/main/clojure"]
  :target-path "target/%s"

  ; These settings enable stricter checks, and are disabled in VCS:
  ;:global-vars {*warn-on-reflection* true}
  ;:pedantic? :abort
  )
