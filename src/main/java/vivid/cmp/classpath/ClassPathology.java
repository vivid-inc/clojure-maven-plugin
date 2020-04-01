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

package vivid.cmp.classpath;

import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import vivid.cmp.components.Resolution;
import vivid.cmp.mojo.AbstractCMPMojo;
import vivid.cmp.mojo.ClojureMojoState;

import java.net.MalformedURLException;
import java.net.URL;

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
            final ClojureMojoState state
    ) throws MojoExecutionException {
        // Dynamically resolve the classpath for the sub-process
        final Set<Artifact> dependencies = Resolution.getResolvedDependencies(
                mojo,
                state
        );

        return Stream
                .ofAll(state.sourcePaths)
                .append(
                        mojo.mavenSession().getCurrentProject().getBuild().getOutputDirectory()
                )
                .appendAll(
                        state.classpathScope == ClasspathScopes.TEST ?
                                Stream.ofAll(state.testPaths)
                                        .append(mojo.mavenSession().getCurrentProject().getBuild().getOutputDirectory())
                                : Stream.empty()
                )
                .appendAll(
                        dependencies.map(d -> d.getFile().getPath())
                );
    }

}
