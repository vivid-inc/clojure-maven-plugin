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

package vivid.cmp.datatypes;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

import static org.apache.maven.artifact.Artifact.SCOPE_COMPILE;
import static org.apache.maven.artifact.Artifact.SCOPE_COMPILE_PLUS_RUNTIME;
import static org.apache.maven.artifact.Artifact.SCOPE_PROVIDED;
import static org.apache.maven.artifact.Artifact.SCOPE_RUNTIME;
import static org.apache.maven.artifact.Artifact.SCOPE_TEST;

/**
 * Selects one of Maven's classpath scopes.
 */
public enum ClasspathScope {

    NONE(
            HashSet.empty()
    ),
    COMPILE(
            HashSet.of(SCOPE_COMPILE, SCOPE_COMPILE_PLUS_RUNTIME, SCOPE_PROVIDED, SCOPE_RUNTIME)
    ),
    TEST(
            HashSet.of(SCOPE_COMPILE, SCOPE_COMPILE_PLUS_RUNTIME, SCOPE_PROVIDED, SCOPE_RUNTIME, SCOPE_TEST)
    );

    public final Set<String> effectiveScopes;

    ClasspathScope(final Set<String> effectiveScopes) {
        this.effectiveScopes = effectiveScopes;
    }

    public String[] effectiveScopesAsJavaArray() {
        return effectiveScopes.toJavaArray(String[]::new);
    }

}
