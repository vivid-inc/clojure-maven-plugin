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

package vivid.cmp;

import vivid.cherimoya.annotation.Constant;

/**
 *
 * Identifiers using CamelCase convention prevalent in Java and Maven
 * rather than dashed convention epitomized by Lisp.
 */
public class Static {

    private Static() {
        // Hide the public constructor
    }

    private static final String DONT_MAKE_ME_THINK =
            "Maintaining forward-compatibility with newer versions of vivid:clojure-maven-plugin " +
                    "is less cost and trouble for plugin users.";

    @Constant(rationale = DONT_MAKE_ME_THINK)
    static final String POM_CMP_CLOJURE_MOJO_NAME = "clojure";

    @Constant(rationale = DONT_MAKE_ME_THINK)
    static final String POM_CMP_ARGS_PROPERTY_KEY = "args";

    @Constant(rationale = "The 'clojure' executable provided by the Clojure distribution " +
            "is and may always be hardcoded as 'clojure'.")
    static final String POM_CMP_CLOJURE_EXECUTABLE_PROPERTY_DEFAULT_VALUE = "clojure";

    @Constant(rationale = DONT_MAKE_ME_THINK)
    static final String POM_CMP_CLOJURE_EXECUTABLE_PROPERTY_KEY = "executable";

    @Constant(rationale = "Fixed at the conventional Maven directory path for Clojure source code")
    static final String[] POM_CMP_CLOJURE_SOURCE_PATHS_DEFAULT_VALUE = new String[] {"src/main/clojure"};

    @Constant(rationale = "Fixed at the conventional Maven directory path for Clojure test code")
    static final String[] POM_CMP_CLOJURE_TEST_PATHS_DEFAULT_VALUE = new String[] {"src/test/clojure"};

    @Constant(rationale = DONT_MAKE_ME_THINK)
    static final String POM_CMP_CLOJURE_SCOPE_PROPERTY_KEY = "mavenScope";

}
