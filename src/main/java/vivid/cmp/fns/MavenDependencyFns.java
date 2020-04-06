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

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolverException;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import vivid.cmp.datatypes.ClasspathScope;
import vivid.cmp.datatypes.ClojureMojoState;
import vivid.cmp.mojo.AbstractCMPMojo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.apache.maven.shared.artifact.filter.resolve.ScopeFilter.including;

public class MavenDependencyFns {

    private MavenDependencyFns() {
        // Hide the public constructor
    }

    public static Set<Artifact> getResolvedDependencies(
            final AbstractCMPMojo mojo,
            final ClojureMojoState state
    ) {
        try {
            final Iterable<ArtifactResult> dependencies =
                    mojo.dependencyResolver().resolveDependencies(
                            mojo.mavenSession().getProjectBuildingRequest(),
                            mojo.mavenSession().getCurrentProject().getDependencies(),
                            mojo.mavenSession().getCurrentProject().getDependencies(),
                            including( state.classpathScope.effectiveScopesAsJavaArray() )
                    );

            return Stream
                    .ofAll(dependencies)
                    .map(ArtifactResult::getArtifact)
                    .collect(HashSet.collector());
        }
        catch (final DependencyResolverException e) {
            // TODO Convert to Either<Message, ...>
            System.out.println(e.getLocalizedMessage());
            System.exit(1);
            return null;
        }
    }

    public static Dependency newDependency(
            final String groupId,
            final String artifactId,
            final String version
    ) {
        final Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        return dependency;
    }

    public static ProjectBuildingRequest newProjectBuildingRequest(
            final ProjectBuildingRequest projectBuildingRequest,
            final List<ArtifactRepository> remoteRepositories
    ) {
        final ProjectBuildingRequest request =
                new DefaultProjectBuildingRequest(
                        projectBuildingRequest
                );
        request.setRemoteRepositories(remoteRepositories);
        return request;
    }

    public static org.eclipse.aether.resolution.ArtifactResult resolveArtifact(
            final AbstractCMPMojo mojo,
            final Dependency dependency
    ) throws ArtifactResolutionException {
        final ArtifactRequest artifactRequest = new ArtifactRequest();

        artifactRequest.setArtifact(toArtifact(dependency));
        artifactRequest.setRepositories(RepositoryUtils.toRepos(mojo.remoteRepositories));

        return mojo.repositorySystem.resolveArtifact(
                mojo.repositorySystemSession,
                artifactRequest
        );
    }

    public static Iterable<ArtifactResult> resolveDependencies(
            final AbstractCMPMojo mojo,
            final Dependency dependency
    ) throws DependencyResolverException, MojoFailureException {
        mojo.getLog().debug("Resolving dependencies for: " + dependency);
        final Iterable<ArtifactResult> dependencyArtifacts =
                mojo.dependencyResolver.resolveDependencies(
                        newProjectBuildingRequest(
                                mojo.mavenSession.getProjectBuildingRequest(),
                                mojo.remoteRepositories
                        ),
                        Collections.singletonList(dependency),
                        Collections.singletonList(dependency),
                        including( ClasspathScope.COMPILE.effectiveScopesAsJavaArray() )
                );

        if (dependencyArtifacts == null || !dependencyArtifacts.iterator().hasNext()) {
            throw new MojoFailureException(
                    "No dependencies were resolved for: " + dependency
            );
        }
        for (final ArtifactResult artifactResult : dependencyArtifacts) {
            mojo.getLog().debug(
                    String.format(
                            "Resolved dependency artifact: %s",
                            artifactResult.getArtifact().getFile().toString()
                    )
            );
        }

        return dependencyArtifacts;
    }

    public static DefaultArtifact toArtifact(
            final Dependency dependency
    ) {
        Objects.requireNonNull(dependency, "dependency is null");
        return new DefaultArtifact(
                String.format(
                        "%s:%s:%s",
                        dependency.getGroupId(),
                        dependency.getArtifactId(),
                        dependency.getVersion()
                )
        );
    }

    public static Predicate<Dependency> inScopeP(
            final ClasspathScope classpathScope
    ) {
        return dependency -> classpathScope.effectiveScopes.contains(dependency.getScope());
    }

}
