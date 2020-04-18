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

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import io.vavr.collection.TreeMap;
import io.vavr.control.Option;
import org.apache.maven.model.Dependency;
import us.bpsm.edn.Keyword;
import us.bpsm.edn.Symbol;
import vivid.cmp.fns.ClassPathology;
import vivid.cmp.fns.MavenDependencyFns;
import vivid.cmp.mojo.AbstractCMPMojo;

import java.util.function.Function;

public class DepsEdn {

    private DepsEdn() {
        // Hide the public constructor
    }

    public static java.util.Map<Keyword, java.util.Map<Keyword, Object>> create(
            final AbstractCMPMojo mojo,
            final Map<String, ClojureMojoState> executionConfigurations
    ) {
        return HashMap.of(
                Keyword.newKeyword("aliases"),
                aliases(mojo, executionConfigurations).toJavaMap()
        ).toJavaMap();
    }

    private static Map<Keyword, Object> aliases(
            final AbstractCMPMojo mojo,
            final Map<String, ClojureMojoState> executionConfigurations
    ) {
        return executionConfigurations.foldLeft(
                TreeMap.empty(),
                (m, executionConfiguration) ->
                        m.put(
                                Keyword.newKeyword(executionConfiguration._1),
                                alias(mojo, executionConfiguration._2).toJavaMap()
                        )
        );
    }

    private static Map<Keyword, Object> alias(
            final AbstractCMPMojo mojo,
            final ClojureMojoState state
    ) {
        return Stream.of(
                Option.of(
                        //Stream<String> paths
                        new Tuple2<Keyword, Object>(
                                Keyword.newKeyword("extra-paths"),
                                ClassPathology.getClassPathForScope(mojo, state, false, ClassPathology.PathStyle.RELATIVE).toJavaList()
                        )
                ),
                Option.of(
                        new Tuple2<Keyword, Object>(
                                Keyword.newKeyword("extra-deps"),
                                deps(
                                        // TODO Package this with the incoming parameters. edn shouldn't know about it.
                                        List.ofAll(mojo.mavenSession().getCurrentProject().getDependencies())
                                                .filter(MavenDependencyFns.inScopeP(state.classpathScope))
                                ).toJavaMap()
                        )
                ),
                Option.when(
                        state.args.isDefined(),
                        new Tuple2<Keyword, Object>(
                                Keyword.newKeyword("main-opts"),
                                state.args.toJavaList()
                        )
                )
        )
                .flatMap(Function.identity())
                .foldLeft(
                        HashMap.empty(),
                        HashMap::put
                );
    }

    private static Map<Symbol, ?> deps(
            final List<Dependency> dependencies
    ) {
        return dependencies
                .map(DepsEdn::artifact2ga)
                .toMap(x -> x._1, x -> x._2);
    }

    private static Tuple2<Symbol, ?> artifact2ga(
            final Dependency dependency
    ) {
        final Symbol ga = Symbol.newSymbol(
                String.join(
                        "/",
                        Stream.of(
                                Option.of(dependency.getGroupId()),
                                Option.of(dependency.getArtifactId())
                        )
                                .flatMap(Function.identity())
                )
        );

        final Map<Keyword, String> version = HashMap.of(
                Keyword.newKeyword("mvn", "version"),
                dependency.getVersion()
        );

        return Map.entry(
                ga,
                version.toJavaMap()
        );
    }

}
