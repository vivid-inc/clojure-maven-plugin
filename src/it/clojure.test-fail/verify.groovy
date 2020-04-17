/*
 * Copyright 2020 The vivid:clojure-maven-plugin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

def buildLog = new File(basedir, "build.log")
assert buildLog.exists()

// Surefire failures
assert buildLog.text.contains("[ERROR] Tests run: 3, Failures: 1, Errors: 1, Skipped: 1")

// CMP failures
assert buildLog.text.contains("ERROR in failure-test/cause-arithmetic-exception")
assert buildLog.text.contains("FAIL in failure-test/documented")
assert buildLog.text.contains("FAIL in failure-test/no-exception-thrown")
assert buildLog.text.contains("FAIL in failure-test/false-assertion")
assert buildLog.text.contains("expected: :yes")
assert buildLog.text.contains("actual: :no")
assert buildLog.text.contains("diff: - {:bar 2}")
assert buildLog.text.contains("+ {:bar 3}")
assert buildLog.text.contains("FAIL in failure-test/multiple-failing-assertions-with-template-expression")
assert buildLog.text.contains("[ERROR] Tests run: 6, Assertions: 7, Failures: 6, Errors: 1, Time elapsed:")
assert buildLog.text.contains("BUILD FAILURE")

def junitReportXml = new File (basedir, "target/clojure-test-reports/all-tests.xml")
assert junitReportXml.exists()
