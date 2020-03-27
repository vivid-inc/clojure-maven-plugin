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

import io.vavr.collection.HashSet;
import io.vavr.collection.Stream;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolverException;

import static org.apache.maven.artifact.Artifact.SCOPE_COMPILE;
import static org.apache.maven.artifact.Artifact.SCOPE_COMPILE_PLUS_RUNTIME;
import static org.apache.maven.artifact.Artifact.SCOPE_RUNTIME;
import static org.apache.maven.artifact.Artifact.SCOPE_TEST;
import static org.apache.maven.shared.artifact.filter.resolve.ScopeFilter.including;

public class ClassPathology {

    private ClassPathology() {
        // Hide the public constructor
    }

    private static final String[] COMPILE_SCOPES = new String[] {SCOPE_COMPILE, SCOPE_COMPILE_PLUS_RUNTIME, SCOPE_RUNTIME};
    private static final String[] TEST_SCOPES = new String[] {SCOPE_COMPILE, SCOPE_COMPILE_PLUS_RUNTIME, SCOPE_RUNTIME, SCOPE_TEST};

    static Stream<String> getClassPathForScope(
            final MojoComponents mojo,
            final MavenScope classPathScope,
            final String[] clojureSourcePaths,
            final String[] clojureTestPaths
    ) throws MojoExecutionException {
        // Dynamically resolve the classpath for the sub-process
        final HashSet<Artifact> dependencies = getResolvedDependencies(
                mojo,
                classPathScope == MavenScope.TEST ?
                        TEST_SCOPES :
                        COMPILE_SCOPES
        );

        return Stream
                .of(clojureSourcePaths)
                .append(
                        mojo.mavenSession().getCurrentProject().getBuild().getOutputDirectory()
                )
                .appendAll(
                        classPathScope == MavenScope.TEST ?
                                Stream.of(clojureTestPaths)
                                        .append(mojo.mavenSession().getCurrentProject().getBuild().getOutputDirectory())
                                : Stream.empty()
                )
                .appendAll(
                        dependencies.map(d -> d.getFile().getPath())
                );
    }

    private static HashSet<Artifact> getResolvedDependencies(
            final MojoComponents mojo,
            final String... resolutionScopes
    )
            throws MojoExecutionException
    {
        try {
            final Iterable<ArtifactResult> dependencies = mojo.dependencyResolver().resolveDependencies(
                    mojo.mavenSession().getProjectBuildingRequest(),
                    mojo.mavenSession().getCurrentProject().getDependencies(),
                    mojo.mavenSession().getCurrentProject().getDependencies(),
                    including( resolutionScopes )
            );

            return Stream
                    .ofAll(dependencies)
                    .map(ArtifactResult::getArtifact)
                    .collect(HashSet.collector());
        }
        catch (final DependencyResolverException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
