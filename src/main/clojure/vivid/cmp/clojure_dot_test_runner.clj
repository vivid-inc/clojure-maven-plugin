(ns vivid.cmp.clojure-dot-test-runner
  (:require
    [clojure.java.io :as io]
    [clojure.string]
    [clojure.test]
    [eftest.report]
    [eftest.report.junit]
    [eftest.report.pretty]
    [eftest.runner]
    [io.aviso.ansi])
  (:import
    (java.io Writer)
    (java.util Map)
    (vivid.cmp.mojo AbstractCMPMojo)))


; Design notes:
;
; The behavior of this Maven goal, as is apparent to the user and tooling, is patterned after maven-surefire-plugin.

; TODO See if data.xml can be used to build the JUnit report (rather than eftest.report.junit) to simplify this implementation


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
; Message formatting
;

(defn format-interval [duration]
  (format "%.3f s" (double (/ duration 1e3))))

(defn pretty-ns
  [ns]
  (let [parts (-> ns (ns-name) (name) (clojure.string/split #"\."))
        normal (or (some->> (butlast parts) (clojure.string/join ".") (format "%s.")) "")]
    (str normal io.aviso.ansi/bold-white-font (last parts) io.aviso.ansi/reset-font)))

(defn log-end-test-ns-summary [data {:keys [test pass fail error]}]
  (let [total (+ pass fail error)
        all-tests-passed (= pass total)
        [log-fn color] (if all-tests-passed
                         [log-info io.aviso.ansi/bold-green-font]
                         [log-error io.aviso.ansi/bold-red-font])
        summary (format
                  (i18n-get-text "vivid.clojure-maven-plugin.action.end-test-ns-summary-format"
                                 ; Note: The total assertions, failures, and error counts are
                                 ; are double-counted as the combined effect of running both
                                 ; the pretty-printer and the JUnit output reporting functions.
                                 (str test) (str (/ pass 2)) (str (/ fail 2)) (str (/ error 2))
                                 (pretty-ns (:ns data)))
                  color
                  io.aviso.ansi/reset-font)]
    (log-fn summary)))



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
                             (pretty-ns (:ns data)))))
  (when (= :end-test-ns (:type data))
    (log-end-test-ns-summary data @clojure.test/*report-counters*))
  (eftest.report.pretty/report data)
  (with-context-writer
    (eftest.report.junit/report data)))

(defmethod cmp-report :summary [{:keys [test pass fail error duration] :as data}]
  ; Handles the :summary type on our own, preventing eftest's own reporters from printing a summary.
  (let [total (+ pass fail error)
        all-tests-passed (= pass total)
        [log-fn color] (if all-tests-passed
                 [log-info io.aviso.ansi/bold-green-font]
                 [log-error io.aviso.ansi/bold-red-font])
        summary [""
                 (i18n-get-text "vivid.clojure-maven-plugin.phrase.results")
                 ""
                 (format
                   (i18n-get-text "vivid.clojure-maven-plugin.action.end-test-run-summary-format"
                                  (str test)
                                  ; Note: The total assertions, failures, and error counts are
                                  ; are double-counted as the combined effect of running both
                                  ; the pretty-printer and the JUnit output reporting functions.
                                  (str (/ total 2)) (str(/ fail 2)) (str (/ error 2))
                                  (format-interval duration))
                   color
                   io.aviso.ansi/reset-font)
                 ""]]
    (when-not all-tests-passed
      ; Ensure at least one blank line separates the non-passing
      ; test output and the summary body using Maven's log.
      (println))
    (doall (map log-fn summary)))
  (with-context-writer
    (eftest.report.junit/report data)))

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
  [^AbstractCMPMojo mojo ^Map options]
  (binding [*context* (atom {})
            *mojo* mojo]
    (let [junit-report-pathname (.get options "junit-report-filename")
          multithread (.get options "multithread")]
      (io/make-parents (io/file junit-report-pathname))
      (swap! *context* assoc :writer (io/writer junit-report-pathname))
      (let [results (some-> (eftest.runner/find-tests "src/test/clojure")    ; TODO pass path in options
                            (found-tests)
                            (eftest.runner/run-tests {:capture-output? false ; TODO pass as configuration parameter
                                                      :multithread? multithread
                                                      :report cmp-report}))
            writer ^Writer (get @*context* :writer)]
        (.close writer)
        (swap! *context* dissoc :writer)
        (all-tests-passed? results)))))
