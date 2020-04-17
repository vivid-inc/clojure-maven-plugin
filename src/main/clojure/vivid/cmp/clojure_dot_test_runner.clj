(ns vivid.cmp.clojure-dot-test-runner
  (:require
    [clojure.java.io :as io]
    [clojure.test]
    [eftest.report]
    [eftest.report.junit]
    [eftest.report.pretty]
    [eftest.runner]
    [io.aviso.ansi])
  (:import
    (java.util Map)
    (vivid.cmp.mojo AbstractCMPMojo)
    (java.io Writer)))


; Design notes:
;
; This task is patterned after maven-surefire-plugin.
;
; When there are no tests to run, maven-surefire-plugin logs to that effect:
;     [INFO] --- maven-surefire-plugin:2.22.2:test (default) @ clojure.test-pass ---
;     [INFO] No tests to run.
; and doesn't write its JUnit XML report.


;
; Console output via Maven's logger
;

(def ^:dynamic *mojo*)

(defn- log-error
  [message]
  (.. *mojo* (getLog) (error message)))
(defn- log-info
  [message]
  (.. *mojo* (getLog) (info message)))

(defn i18n-get-text
  [key & args]
  (let [i18n-context (.i18nContext *mojo*)]
    (.getText i18n-context key (into-array Object args))))



;
; clojure.test -compatible reporting
;

(def ^:dynamic *context* nil)

(defmacro with-context-writer [& body]
  `(let [writer# (get @*context* :writer)]
     (binding [clojure.test/*test-out* writer#]
       ~@body)))

(defmulti cmp-report
          "clojure.test -compatible reporting multi-methods.
          Results are logged to the console written to a JUnit XML reporting file;
          both accomplished by delegating to each of 'eftest's respective
          reporting facilities."
          :type)

(defmethod cmp-report :default [data]
  (when (= :begin-test-ns (:type data))
    (log-info (i18n-get-text "vivid.clojure-maven-plugin.action.running-test"
                             (ns-name (:ns data)))))
  (eftest.report.pretty/report data)
  (with-context-writer
    (eftest.report.junit/report data)))

(defn format-interval [duration]
  (format "%.3f s" (double (/ duration 1e3))))

(defmethod cmp-report :summary [{:keys [test pass fail error duration]}]
  (let [total (+ pass fail error)
        all-tests-passed (= pass total)
        [log-fn color] (if all-tests-passed
                 [log-info io.aviso.ansi/bold-green-font]
                 [log-error io.aviso.ansi/bold-red-font])
        summary [""
                 (i18n-get-text "vivid.clojure-maven-plugin.phrase.results")
                 ""
                 (format
                   (i18n-get-text "vivid.clojure-maven-plugin.action.test-run-summary-format"
                                  test
                                  ; Note: The total assertions, failures, and error counts are
                                  ; are double-counted as the combined effect of running both
                                  ; the pretty-printer and the JUnit output reporting functions.
                                  (/ total 2) (/ fail 2) (/ error 2)
                                  (format-interval duration))
                   color
                   io.aviso.ansi/reset-font)
                 ""]]
    (when-not all-tests-passed
      ; Ensure at least one blank line separates the non-passing
      ; test output and the summary body using Maven's log.
      (println))
    (doall (map log-fn summary))))

; TODO Report per-testsuite metrics to the console:
;     [INFO] Running vivid.lib.messages.MessageResourceTest
;     [INFO] Tests run: 1490, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.518 s - in vivid.lib.messages.MessageResourceTest


;
; Test runner
;

(defn found-tests
  [xs]
  (if (empty? xs)
    (log-info (i18n-get-text "vivid.clojure-maven-plugin.action.no-tests-found"))
    xs))

(defn all-tests-passed?
  [results]
  (if results
    (let [{pass :pass
           fail :fail
           error :error} results
          total (+ pass fail error)]
      (= pass total))
    true))

(defn run-tests
  "Run tests compatible with clojure.test.
  Returns a boolean indicating whether all tests have passed."
  ; TODO Use data.xml to build the report, drastically cleaning up this fn's implementation
  [^AbstractCMPMojo mojo ^Map options]
  (binding [*context* (atom {})
            *mojo* mojo]
    (let [junit-report-pathname (.get options "junit-report-filename")
          multithread (.get options "multithread")]
      (io/make-parents (io/file junit-report-pathname))
      (swap! *context* assoc :writer (io/writer junit-report-pathname))
      (let [results (some-> (eftest.runner/find-tests "src/test/clojure")    ; TODO pass path as an option
                            (found-tests)
                            (eftest.runner/run-tests {:multithread? multithread
                                                      :report cmp-report}))
            writer ^Writer (get @*context* :writer)]
        (.close writer)
        (swap! *context* dissoc :writer)
        (all-tests-passed? results)))))
