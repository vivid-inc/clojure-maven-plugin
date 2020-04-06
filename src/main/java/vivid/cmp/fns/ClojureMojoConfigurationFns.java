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

package vivid.cmp.fns;

import io.vavr.Function2;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import vivid.cmp.datatypes.ClasspathScope;
import vivid.cmp.datatypes.ClojureMojoState;
import vivid.cmp.messages.Message;
import vivid.cmp.messages.VCMPE1InternalError;

import static vivid.cmp.datatypes.ClojureMojoState.CLOJURE_ARGS_PARAMETER_KEY;
import static vivid.cmp.datatypes.ClojureMojoState.CLOJURE_CLASSPATHSCOPE_PARAMETER_KEY;
import static vivid.cmp.datatypes.ClojureMojoState.CLOJURE_EXECUTABLE_PARAMETER_KEY;
import static vivid.cmp.datatypes.ClojureMojoState.CLOJURE_SOURCEPATHS_PARAMETER_KEY;
import static vivid.cmp.datatypes.ClojureMojoState.CLOJURE_SOURCEPATH_PARAMETER_CHILDNAME_KEY;
import static vivid.cmp.datatypes.ClojureMojoState.CLOJURE_TESTPATHS_PARAMETER_KEY;
import static vivid.cmp.datatypes.ClojureMojoState.CLOJURE_TESTPATH_PARAMETER_CHILDNAME_KEY;

public class ClojureMojoConfigurationFns {

    private ClojureMojoConfigurationFns() {
        // Hide the public constructor
    }

    private static Function2<String, Xpp3Dom, List<String>> getChildrenValues =
            (childName, dom) -> Stream.of(dom.getChildren())
                    .filter(child -> childName.equalsIgnoreCase(child.getName()))
                    .map(Xpp3Dom::getValue)
                    .toList();

    private static Function2<Either<Message, ClojureMojoState>, Xpp3Dom, Either<Message, ClojureMojoState>>
            updateInPlaceClojureMojoState =
            (state, dom) -> {
                if (state.isLeft()) {
                    return state;
                }
                final String key = dom.getName();
                switch (key) {
                    case CLOJURE_ARGS_PARAMETER_KEY:
                        return Either.right(state.get().setArgs(Option.of(dom.getValue())));
                    case CLOJURE_EXECUTABLE_PARAMETER_KEY:
                        return Either.right(state.get().setExecutable(dom.getValue()));
                    case CLOJURE_CLASSPATHSCOPE_PARAMETER_KEY:
                        return Either.right(state.get().setClasspathScope(
                                ClasspathScope.valueOf(dom.getValue()))
                        );
                    case CLOJURE_SOURCEPATHS_PARAMETER_KEY:
                        return Either.right(state.get().setSourcePaths(
                                getChildrenValues.apply(CLOJURE_SOURCEPATH_PARAMETER_CHILDNAME_KEY, dom))
                        );
                    case CLOJURE_TESTPATHS_PARAMETER_KEY:
                        return Either.right(state.get().setTestPaths(
                                getChildrenValues.apply(CLOJURE_TESTPATH_PARAMETER_CHILDNAME_KEY, dom))
                        );
                    default:
                        // Ignore this unknown field
                        // TODO log.debug(): "Unknown " + ClojureMojoState.class.getName() + " field name: " + key
                        return state;
                }
            };

    public static Either<Message, ClojureMojoState> asClojureMojoState(
            final PluginExecution pluginExecution
    ) {
        final Object untyped = pluginExecution.getConfiguration();
        if (!(untyped instanceof Xpp3Dom)) {
            return Either.left(
                    VCMPE1InternalError.message(
                            "PluginExecution.getConfiguration() returned an " +
                                    "object of an unhandled type: " + untyped.getClass()
                    )
            );
        }

        return
                Stream.of(((Xpp3Dom) untyped).getChildren())
                        .foldLeft(
                                Either.right(ClojureMojoState.DEFAULT_STATE),
                                updateInPlaceClojureMojoState
                        );
    }

}
