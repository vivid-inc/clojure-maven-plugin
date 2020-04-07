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
import vivid.cherimoya.annotation.Constant;
import vivid.cmp.datatypes.ClasspathScope;
import vivid.cmp.datatypes.ClojureMojoState;
import vivid.cmp.fns.ClassPathology;
import vivid.cmp.fns.SubProcessFns;

import java.util.ArrayList;

import static vivid.cmp.datatypes.ClojureMojoState.CLOJURE_ARGS_PARAMETER_KEY;
import static vivid.cmp.datatypes.ClojureMojoState.CLOJURE_CLASSPATHSCOPE_PARAMETER_KEY;
import static vivid.cmp.datatypes.ClojureMojoState.CLOJURE_EXECUTABLE_PARAMETER_KEY;
import static vivid.cmp.datatypes.ClojureMojoState.CLOJURE_SOURCEPATHS_PARAMETER_KEY;
import static vivid.cmp.datatypes.ClojureMojoState.CLOJURE_SOURCEPATHS_PROPERTY_DEFAULT_VALUE;
import static vivid.cmp.datatypes.ClojureMojoState.CLOJURE_TESTPATHS_PARAMETER_KEY;
import static vivid.cmp.datatypes.ClojureMojoState.CLOJURE_TESTPATHS_PROPERTY_DEFAULT_VALUE;

/**
 * Executes 'clojure' as a sub-process with a variety of options.
 * If 'clojure' returns a non-zero exit value, it is wrapped in an
 * Exception and fed back to Maven.
 *
 * @since 0.2.0
 */
@Mojo(
        name = AbstractCMPMojo.CLOJURE_MOJO_GOAL_NAME
)
public class ClojureMojo extends AbstractCMPMojo {


    @Constant
    private static final String CLOJURE_GOAL_PROPERTY_KEY_PREFIX =
            AbstractCMPMojo.CLOJURE_MOJO_GOAL_NAME + ".";

    @Constant
    private static final String CLOJURE_ARGS_PROPERTY_KEY =
            CLOJURE_GOAL_PROPERTY_KEY_PREFIX + CLOJURE_ARGS_PARAMETER_KEY;

    @Constant
    private static final String CLOJURE_CLASSPATHSCOPE_PROPERTY_KEY =
            CLOJURE_GOAL_PROPERTY_KEY_PREFIX + CLOJURE_CLASSPATHSCOPE_PARAMETER_KEY;

    @Constant
    private static final String CLOJURE_EXECUTABLE_PROPERTY_KEY =
            CLOJURE_GOAL_PROPERTY_KEY_PREFIX + CLOJURE_EXECUTABLE_PARAMETER_KEY;

    @Constant
    private static final String CLOJURE_SOURCEPATHS_PROPERTY_KEY =
            CLOJURE_GOAL_PROPERTY_KEY_PREFIX + CLOJURE_SOURCEPATHS_PARAMETER_KEY;

    @Constant
    private static final String CLOJURE_TESTPATHS_PROPERTY_KEY =
            CLOJURE_GOAL_PROPERTY_KEY_PREFIX + CLOJURE_TESTPATHS_PARAMETER_KEY;


    //
    // User-provided configuration
    //

    /**
     * Arguments to 'clojure'. These are added to the arguments provided by this Maven plugin.
     */
    @Parameter(property = CLOJURE_ARGS_PROPERTY_KEY)
    private String args = ClojureMojoState.DEFAULT_STATE.args.getOrElse((String) null);

    /**
     * Specifies how to configure the run-time classpath in the 'clojure' sub-process,
     * according to Maven's notion of the current project's scoped elements.
     * 'COMPILE' specifies compile- and runtime-scoped dependencies, source directories,
     * and their output directories.
     * 'TEST' will add test-scoped dependencies, test source directories, and test
     * output directories.
     */
    @Parameter(property = CLOJURE_CLASSPATHSCOPE_PROPERTY_KEY)
    private ClasspathScope classpathScope = ClojureMojoState.DEFAULT_STATE.classpathScope;

    /**
     * The path to the 'clojure' executable. Without explicitly setting this parameter,
     * the plugin expects 'clojure' to be available on the path. A specific path to anything
     * at all can be specified here, including to something other than 'clojure'.
     */
    @Parameter(property = CLOJURE_EXECUTABLE_PROPERTY_KEY)
    private String executable = ClojureMojoState.DEFAULT_STATE.executable;

    /**
     * Specifies paths containing Clojure source code to be added to the
     * classpath for 'clojure'.
     */
    @Parameter(property = CLOJURE_SOURCEPATHS_PROPERTY_KEY, defaultValue = CLOJURE_SOURCEPATHS_PROPERTY_DEFAULT_VALUE)
    private java.util.List<String> sourcePaths = new ArrayList<>();

    /**
     * Specifies paths containing Clojure test source code to be added to
     * the classpath for 'clojure'.
     */
    @Parameter(property = CLOJURE_TESTPATHS_PROPERTY_KEY, defaultValue = CLOJURE_TESTPATHS_PROPERTY_DEFAULT_VALUE)
    private java.util.List<String> testPaths = new ArrayList<>();


    @SuppressWarnings("java:S5304")
    @Override
    public void execute()
            throws MojoExecutionException {
        super.initialize();

        final ClojureMojoState state = new ClojureMojoState(
                Option.of(args),
                executable,
                classpathScope,
                List.ofAll(sourcePaths),
                List.ofAll(testPaths)
        );

        // Execute 'clojure'
        SubProcessFns.executeSubProcess(
                this,

                // User-overridable path to the `clojure' CLI executable
                executable,

                // User-provided arguments, if any
                args,

                // Calculated depending on the user-selectable Maven scope,
                // 'compile' or 'test' for example
                ClassPathology.getClassPathForScope(
                        this,
                        state,
                        true),

                // The sub-process inherits the same environment variables
                // as the executing Maven process
                System.getenv()
        );
    }

}
