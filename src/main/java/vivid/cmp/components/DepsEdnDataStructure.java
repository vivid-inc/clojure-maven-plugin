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

package vivid.cmp.components;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import org.apache.maven.artifact.Artifact;
import us.bpsm.edn.Keyword;
import us.bpsm.edn.Symbol;

import java.util.function.Function;

public class DepsEdnDataStructure {

    private DepsEdnDataStructure() {
        // Hide the public constructor
    }

    public static java.util.Map<Keyword, Object> create(
            final Stream<String> paths,
            final Set<Artifact> resolvedDependencies
    ) {
        return HashMap.of(
                Keyword.newKeyword("deps"), deps(resolvedDependencies).toJavaMap(),

                Keyword.newKeyword("paths"), paths.toJavaList(),

                // TODO arguments -> aliases
                Keyword.newKeyword("main-opts"), List.of("-m", "hello-world").toJavaList()
        ).toJavaMap();
    }

    private static Map<Symbol, ?> deps(
            final Set<Artifact> resolvedDependencies
    ) {
        return resolvedDependencies
                .map(DepsEdnDataStructure::artifact2ga)
                .toMap(x -> x._1, x -> x._2);
    }

    private static Tuple2<Symbol, ?> artifact2ga(
            final Artifact artifact
    ) {
        final Symbol ga = Symbol.newSymbol(
                String.join(
                        ":",
                        Stream.of(
                                Option.of(artifact.getGroupId()),
                                Option.of(artifact.getArtifactId())
                        )
                                .flatMap(Function.identity())
                )
        );

        final Map<Keyword, String> version = HashMap.of(
                Keyword.newKeyword("mvn", "version"),
                artifact.getVersion()
        );

        return Map.entry(
                ga,
                version.toJavaMap()
        );
    }

}
