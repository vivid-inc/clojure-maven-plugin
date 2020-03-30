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

package vivid.cmp.mojo;

import io.vavr.collection.List;
import io.vavr.control.Option;
import vivid.cherimoya.annotation.Constant;
import vivid.cmp.classpath.ClasspathScopes;

import java.util.function.Consumer;

/**
 * Immutable representation of the state of a {@code vivid:clojure-maven-plugin:clojure}
 * Maven goal execution configuration.
 * Provides mutator methods that return an updated, immutable copy of this state instance.
 */
public class ClojureMojoState {

    @Constant
    static final Option<String> CLOJURE_ARGS_PROPERTY_DEFAULT_VALUE = Option.none();

    @Constant(rationale = "The 'clojure' executable provided by the Clojure distribution " +
            "is and may always be hardcoded as 'clojure'.")
    static final String CLOJURE_EXECUTABLE_PROPERTY_DEFAULT_VALUE = "clojure";

    @Constant
    static final ClasspathScopes CLOJURE_CLASSPATHSCOPE_PROPERTY_DEFAULT_VALUE = ClasspathScopes.COMPILE;

    @Constant(rationale = "Fixed at the conventional Maven directory path for Clojure source code")
    static final List<String> CLOJURE_SOURCEPATHS_PROPERTY_DEFAULT_VALUE = List.of("src/main/clojure");

    @Constant(rationale = "Fixed at the conventional Maven directory path for Clojure test code")
    static final List<String> CLOJURE_TESTPATHS_PROPERTY_DEFAULT_VALUE = List.of("src/test/clojure");

    public final Option<String> args;
    public final String executable;
    public final ClasspathScopes classpathScope;
    public final List<String> sourcePaths;
    public final List<String> testPaths;

    public static final ClojureMojoState DEFAULT_STATE = new ClojureMojoState(
            CLOJURE_ARGS_PROPERTY_DEFAULT_VALUE,
            CLOJURE_EXECUTABLE_PROPERTY_DEFAULT_VALUE,
            CLOJURE_CLASSPATHSCOPE_PROPERTY_DEFAULT_VALUE,
            CLOJURE_SOURCEPATHS_PROPERTY_DEFAULT_VALUE,
            CLOJURE_TESTPATHS_PROPERTY_DEFAULT_VALUE
    );

    ClojureMojoState(
            final Option<String> args,
            final String executable,
            final ClasspathScopes classpathScope,
            final List<String> sourcePaths,
            final List<String> testPaths
    ) {
        this.args = args;
        this.executable = executable;
        this.classpathScope = classpathScope;
        this.sourcePaths = sourcePaths;
        this.testPaths = testPaths;
    }

    private static class State {
        private Option<String> args;
        private String executable;
        private ClasspathScopes classpathScope;
        private List<String> sourcePaths;
        private List<String> testPaths;

        State(final ClojureMojoState original) {
            this.args = original.args;
            this.executable = original.executable;
            this.classpathScope = original.classpathScope;
            this.sourcePaths = original.sourcePaths;
            this.testPaths = original.testPaths;
        }
    }

    private static ClojureMojoState mutate(
            final ClojureMojoState original,
            final Consumer<State> func
    ) {
        final State state = new State(original);
        func.accept(state);
        return new ClojureMojoState(
                state.args,
                state.executable,
                state.classpathScope,
                state.sourcePaths,
                state.testPaths
        );
    }

    /**
     * @return a copy of {@code state} but with its {@code args} field
     *     set to the value of the {@param args} parameter
     */
    ClojureMojoState setArgs(final Option<String> args) {
        return mutate(this, s -> s.args = args);
    }

    ClojureMojoState setExecutable(final String executable) {
        return mutate(this, s -> s.executable = executable);
    }

    ClojureMojoState setClasspathScope(final ClasspathScopes classpathScope) {
        return mutate(this, s -> s.classpathScope = classpathScope);
    }

    ClojureMojoState setSourcePaths(final List<String> sourcePaths) {
        return mutate(this, s -> s.sourcePaths = sourcePaths);
    }

    ClojureMojoState setTestPaths(final List<String> testPaths) {
        return mutate(this, s -> s.testPaths = testPaths);
    }

}
