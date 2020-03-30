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

import io.vavr.collection.List;
import io.vavr.control.Option;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import vivid.cmp.classpath.ClassPathology;
import vivid.cmp.classpath.ClasspathScopes;
import vivid.cmp.components.SubProcess;

/**
 * Executes 'clojure' as a sub-process with a variety of options.
 * If 'clojure' returns a non-zero exit value, it is wrapped in an
 * Exception and fed back to Maven.
 *
 * @since 0.1.0
 */
@Mojo(
        name = AbstractCMPMojo.POM_CMP_CLOJURE_MOJO_GOAL_NAME
)
public class ClojureMojo extends AbstractCMPMojo {


    //
    // User-provided configuration
    //

    /**
     * Arguments to 'clojure'. These are added to the arguments provided by this Maven plugin.
     */
    @Parameter
    private String args = ClojureMojoState.DEFAULT_STATE.args.getOrElse((String) null);

    /**
     * The path to the 'clojure' executable. Without explicitly setting this parameter,
     * the plugin expects 'clojure' to be available on the path. A specific path of anything
     * at all can be specified here, including to something other than 'clojure'.
     */
    @Parameter
    private String executable = ClojureMojoState.DEFAULT_STATE.executable;

    /**
     * Specifies how to configure the run-time classpath in the 'clojure' sub-process,
     * according to Maven's notion of the current project's scoped elements.
     * 'COMPILE' specifies compile- and runtime-scoped dependencies, source directories,
     * and their output directories.
     * 'TEST' will add test-scoped dependencies, test source directories, and test
     * output directories.
     */
    @Parameter
    private ClasspathScopes classpathScope = ClojureMojoState.DEFAULT_STATE.classpathScope;

    /**
     * Specifies paths containing Clojure source code to be added to the
     * classpath for 'clojure'.
     */
    @Parameter
    private String[] sourcePaths = ClojureMojoState.DEFAULT_STATE.sourcePaths.toJavaArray(String[]::new);

    /**
     * Specifies paths containing Clojure test source code to be added to
     * the classpath for 'clojure'.
     */
    @Parameter
    private String[] testPaths = ClojureMojoState.DEFAULT_STATE.testPaths.toJavaArray(String[]::new);


    @Override
    public void execute()
            throws MojoExecutionException {
        super.initialize();

        final ClojureMojoState state = new ClojureMojoState(
                Option.of(args),
                executable,
                classpathScope,
                List.of(sourcePaths),
                List.of(testPaths)
        );

        // TODO 'clojure.test & junit'
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
                executable,

                // User-provided arguments, if any
                args,

                // Calculated depending on the user-selectable Maven scope,
                // 'compile' or 'test' for example
                ClassPathology.getClassPathForScope(
                        this,
                        state
                ),

                // The sub-process inherits the same environment variables
                // as the executing Maven process
                System.getenv()
        );
    }

}
