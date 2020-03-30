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

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolver;
import org.codehaus.plexus.i18n.I18N;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import vivid.cherimoya.annotation.Constant;
import vivid.cmp.messages.I18nContext;

import java.util.List;

public abstract class AbstractCMPMojo extends AbstractMojo {

    static final String DONT_MAKE_ME_THINK =
            "Maintaining forward-compatibility with newer versions of vivid:clojure-maven-plugin " +
                    "is less cost and trouble for plugin users.";
    @Constant(rationale = DONT_MAKE_ME_THINK)
    static final String POM_CMP_CLOJURE_MOJO_GOAL_NAME = "clojure";
    @Constant(rationale = DONT_MAKE_ME_THINK)
    static final String POM_CMP_LEININGEN_GOAL_NAME = "leiningen";
    @Constant(rationale = DONT_MAKE_ME_THINK)
    static final String POM_CMP_WRITE_DEPS_EDN_MOJO_GOAL_NAME = "write-deps-edn";
    @Constant(rationale = DONT_MAKE_ME_THINK)
    static final String POM_CMP_ARGS_PROPERTY_KEY = "args";
    @Constant(rationale = DONT_MAKE_ME_THINK)
    static final String POM_CMP_CLOJURE_EXECUTABLE_PROPERTY_KEY = "executable";
    @Constant(rationale = DONT_MAKE_ME_THINK)
    static final String POM_CMP_CLOJURE_SCOPE_PROPERTY_KEY = "mavenScope";

    //
    // Maven components
    //

    @Component
    public DependencyResolver dependencyResolver;

    @Component
    protected I18N i18n;

    protected I18nContext i18nContext;

    @Parameter(required = true, readonly = true, property = "session")
    public MavenSession mavenSession;

    @Component
    public RepositorySystem repositorySystem;

    @Parameter(required = true, readonly = true, property = "repositorySystemSession")
    public RepositorySystemSession repositorySystemSession;

    @Parameter(required = true, readonly = true, property = "project.remoteArtifactRepositories")
    public List<ArtifactRepository> remoteRepositories;


    /**
     * Note: Until we learn a better way for AbstractCMPMojo to correctly initialize
     * the i18nContext field (is via constructor safe?), sub-classes must call
     * this method as soon as possible.
     */
    protected void initialize() {
        i18nContext = new I18nContext(i18n);
    }


    public DependencyResolver dependencyResolver() {
        return dependencyResolver;
    }
    public I18nContext i18nContext() {
        return i18nContext;
    }
    public MavenSession mavenSession() {
        return mavenSession;
    }

}
