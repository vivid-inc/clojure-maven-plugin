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

import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import io.vavr.collection.TreeMap;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import vivid.cmp.datatypes.ClojureMojoState;
import vivid.cmp.messages.Message;
import vivid.cmp.mojo.AbstractCMPMojo;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class MojoFns {

    private MojoFns() {
        // Hide the public constructor
    }

    public static Predicate<PluginExecution> hasGoalOfName(
            final String goalName
    ) {
        return e ->
                List.ofAll(e.getGoals())
                        .find(goalName::equalsIgnoreCase)
                        .isDefined();
    }

    /*
     * NOTE: This implementation as an expression of a combinator feels too
     * heavy to be an ideal application of VAVR... Am I, its author, not mistaken somewhere?
     */
    private static BiFunction<
            Either<Message, Map<String, ClojureMojoState>>,
            PluginExecution,
            Either<Message, Map<String, ClojureMojoState>>>
    mappedPluginExecutionStateCombinator() {
        return (m, pluginExecution) -> {
            if (m.isLeft()) {
                return m;
            }
            final Either<Message, Option<ClojureMojoState>> state =
                    ClojureMojoConfigurationFns.asClojureMojoState(pluginExecution);
            if (state.isLeft()) {
                return Either.left(state.getLeft());
            }
            if (state.get().isEmpty()) {
                return m;
            }
            return Either.right(
                    m.get().put(
                            pluginExecution.getId(),
                            state.get().get()
                    )
            );
        };
    }

    public static Either<Message, Map<String, ClojureMojoState>> myPluginExecutionConfigurations(
            final AbstractCMPMojo mojo,
            final String pluginLookupKey,
            final String goalName
    ) {
        final Plugin myself = mojo.mavenSession.getCurrentProject().getPlugin(
                pluginLookupKey
        );
        return
                Stream.ofAll(myself.getExecutions())
                        .filter(hasGoalOfName(goalName))
                        .foldLeft(
                                Either.right(TreeMap.empty()),
                                mappedPluginExecutionStateCombinator()
                        );
    }

}
