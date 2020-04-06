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

import io.vavr.collection.Stream;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import vivid.cmp.datatypes.ClasspathScope;
import vivid.cmp.datatypes.ClojureMojoState;
import vivid.cmp.mojo.AbstractCMPMojo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Function;

public class ClassPathology {

    private ClassPathology() {
        // Hide the public constructor
    }

    @SuppressWarnings("java:S2095")
    public static void addToClassLoader(
            final AbstractCMPMojo mojo,
            final org.eclipse.aether.resolution.ArtifactResult artifact,
            final Iterable<ArtifactResult> artifacts
    ) throws DuplicateRealmException, MalformedURLException {
        // Referencing https://webtide.com/extending-the-maven-plugin-classpath-at-runtime/

        final ClassWorld world = new ClassWorld();
        final ClassRealm realm = world.newRealm(
                "vivid.clojure-maven-plugin-with-leiningen",
                Thread.currentThread().getContextClassLoader()
        );

        realm.addURL(
                artifact.getArtifact().getFile().toURI().toURL() // TODO add to stream
        );

        for (final ArtifactResult a : artifacts) {
            final URL url = a.getArtifact().getFile().toURI().toURL();
            realm.addURL(url);
            mojo.getLog().debug(
                    String.format(
                            "Adding to new ClassRealm: %s",
                            url
                    )
            );
        }

        Thread.currentThread().setContextClassLoader(realm);
    }

    public static Stream<String> getClassPathForScope(
            final AbstractCMPMojo mojo,
            final ClojureMojoState state,
            boolean includeTransitiveDependencies
    ) {
        return Stream
                .ofAll(state.sourcePaths)
                .append(
                        mojo.mavenSession().getCurrentProject().getBuild().getOutputDirectory()
                )
                .appendAll(
                        state.classpathScope == ClasspathScope.TEST ?
                                Stream.ofAll(state.testPaths)
                                        .append(mojo.mavenSession().getCurrentProject().getBuild().getOutputDirectory())
                                : Stream.empty()
                )
                .appendAll(
                        includeTransitiveDependencies ?
                                // Dynamically resolve the classpath for the sub-process
                                MavenDependencyFns.getResolvedDependencies(
                                        mojo,
                                        state
                                ).map(d -> d.getFile().getPath()) :
                                Stream.empty()
                )
                .map(
                        relativePath(mojo.mavenSession().getCurrentProject().getBasedir().toPath())
                );
    }

    private static Function<String, String> relativePath(
            final Path base
    ) {
        return fileStr -> base
                .toAbsolutePath()
                .relativize( new File(fileStr).toPath().toAbsolutePath() )
                .toString();
    }

}
