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

public class Static {

    private Static() {
        // Hide the public constructor
    }

    private static final String DONT_MAKE_ME_THINK =
            "Users prefer their existing Maven POM vivid:clojure-maven-plugin configuration to " +
                    "remain compatible as-is with newer versions of this Maven plugin.";

    @Constant(rationale = DONT_MAKE_ME_THINK)
    static final String POM_CMP_CLOJURE_MOJO_NAME = "clojure";


    //
    // Property keys
    //

    @Constant(rationale = DONT_MAKE_ME_THINK)
    static final String POM_CMP_ARGS_PROPERTY_KEY = "args";

    @Constant(rationale = "The 'clojure' executable provided by the Clojure distribution " +
            "is and may always be hardcoded as 'clojure'.")
    static final String POM_CMP_CLOJURE_EXECUTABLE_PROPERTY_DEFAULT_VALUE = "clojure";

    @Constant(rationale = DONT_MAKE_ME_THINK)
    static final String POM_CMP_CLOJURE_EXECUTABLE_PROPERTY_KEY = "executable";

}
