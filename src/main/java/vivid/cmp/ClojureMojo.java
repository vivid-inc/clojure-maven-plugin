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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolver;
import org.codehaus.plexus.i18n.I18N;

/**
 * Executes 'clojure' as a sub-process with a variety of options.
 * If 'clojure' returns a non-zero exit value, it is wrapped in an
 * Exception and fed back to Maven.
 *
 * @since 0.1.0
 */
@Mojo(
        name = Static.POM_CMP_CLOJURE_MOJO_NAME
)
public class ClojureMojo extends AbstractMojo
        implements MojoComponents {

    private I18nContext i18nContext;


    @Component
    private DependencyResolver dependencyResolver;

    @Component
    private I18N i18n;

    @Parameter(required = true, readonly = true, property = "session")
    private MavenSession mavenSession;


    @Override
    public DependencyResolver dependencyResolver() {
        return dependencyResolver;
    }
    @Override
    public I18nContext i18nContext() {
        return i18nContext;
    }
    @Override
    public Log log() {
        return getLog();
    }
    @Override
    public MavenSession mavenSession() {
        return mavenSession;
    }


    //
    // User-provided configuration
    //

    /**
     * Arguments to 'clojure'. These are added to the arguments provided by this Maven plugin.
     */
    @Parameter(alias = Static.POM_CMP_ARGS_PROPERTY_KEY)
    private String clojureArguments;

    /**
     * The path to the 'clojure' executable. Without explicitly setting this parameter,
     * the plugin expects 'clojure' to be available on the path. A specific path of anything
     * at all can be specified here, including to something other than 'clojure'.
     */
    @Parameter(
            alias = Static.POM_CMP_CLOJURE_EXECUTABLE_PROPERTY_KEY,
            defaultValue = Static.POM_CMP_CLOJURE_EXECUTABLE_PROPERTY_DEFAULT_VALUE
    )
    private String clojureExecutable;

    /**
     * Specifies how to configure the run-time classpath in the 'clojure' sub-process,
     * according to Maven's notion of the current project's scoped elements.
     * 'COMPILE' specifies compile- and runtime-scoped dependencies, source directories,
     * and their output directories.
     * 'TEST' will add test-scoped dependencies, test source directories, and test
     * output directories.
     */
    @Parameter(alias = Static.POM_CMP_CLOJURE_SCOPE_PROPERTY_KEY, defaultValue = "COMPILE")
    private MavenScope clojureClassPathScope;

    /**
     * Specifies paths containing Clojure source code to be added to the classpath for 'clojure'.
     */
    @Parameter
    private String[] clojureSourcePaths = Static.POM_CMP_CLOJURE_SOURCE_PATHS_DEFAULT_VALUE;

    /**
     * Specifies paths containing Clojure test source code to be added to the classpath for 'clojure'.
     */
    @Parameter
    private String[] clojureTestPaths = Static.POM_CMP_CLOJURE_TEST_PATHS_DEFAULT_VALUE;


    public void execute()
            throws MojoExecutionException {

        i18nContext = new I18nContext(i18n);

        // Referencing
        // https://github.com/cognitect-labs/test-runner/blob/master/deps.edn
        // https://oli.me.uk/clojure-and-clojurescript-testing-with-the-clojure-cli/
        // https://github.com/ingesolvoll/lein-maven-plugin
        // https://github.com/redbadger/test-report-junit-xml

        // Run the clojure.test tests.
        // Write JUnit report files.
        // Report results on console and back to Maven ala surefire.

        // Execute 'clojure'
        SubProcess.executeSubProcess(
                this,

                // User-overridable path to the `clojure' CLI executable
                clojureExecutable,

                // User-provided arguments, if any
                clojureArguments,

                // Calculated depending on the user-selectable Maven scope,
                // 'compile' or 'test' for example
                ClassPathology.getClassPathForScope(
                        this,
                        clojureClassPathScope,
                        clojureSourcePaths,
                        clojureTestPaths
                ),

                // The sub-process inherits the same environment variables
                // as the executing Maven process
                System.getenv()
        );
    }

}
