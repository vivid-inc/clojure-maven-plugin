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

import clojure.java.api.Clojure;
import clojure.lang.RT;
import clojure.lang.Symbol;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Either;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import vivid.cmp.fns.ClassPathology;
import vivid.cmp.fns.MavenDependencyFns;
import vivid.cmp.messages.Message;
import vivid.cmp.messages.VCMPE1InternalError;
import vivid.polypara.annotation.Constant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static vivid.cmp.datatypes.ClojureMojoState.CLOJURE_ARGS_PARAMETER_KEY;

/**
 * Runs clojure.test tests and reports results with JUnit-compatible output.
 *
 * @since 0.3.0
 */
@Mojo(
        name = AbstractCMPMojo.CLOJURE_TEST_MOJO_GOAL_NAME,
        defaultPhase = LifecyclePhase.TEST
)
public class ClojureTestMojo extends AbstractCMPMojo {


    @Constant
    private static final String CLOJURE_TEST_GOAL_PROPERTY_KEY_PREFIX =
            CLOJURE_MAVEN_PLUGIN_ID + "." + CLOJURE_TEST_MOJO_GOAL_NAME + ".";

    @Constant
    private static final String CLOJURE_TEST_NAMESPACES_PARAMETER_KEY = "namespaces";
    @Constant
    private static final String CLOJURE_TEST_NAMESPACES_PROPERTY_KEY =
            CLOJURE_TEST_GOAL_PROPERTY_KEY_PREFIX + CLOJURE_ARGS_PARAMETER_KEY;

    @Constant
    private static final String CLOJURE_TEST_SKIP_PARAMETER_KEY = "skip";
    @Constant
    private static final String CLOJURE_TEST_SKIP_PROPERTY_KEY =
            CLOJURE_TEST_GOAL_PROPERTY_KEY_PREFIX + CLOJURE_TEST_SKIP_PARAMETER_KEY;


    //
    // User-provided configuration
    //

    /**
     * ID of a {@code clojure} goal
     */
    @Parameter
    private String clojureConfigurationId;

    @Parameter(defaultValue = "${maven.test.skip}", property = CLOJURE_TEST_SKIP_PROPERTY_KEY)
    private boolean skip;






    // TODO maven.test.failure.ignore

    // TODO maven configuration id of clojure configuration to build

    // read with Clojure read-string. symbols, regexes, fns.
//    @Parameter(property = CLOJURE_TEST_NAMESPACES_PROPERTY_KEY)
//    private java.util.List<String> namespaces = new ArrayList<>();
    // TODO namespace symbol, regex matching ns, metadata-keywords (think "labels")
    // TODO choose by fn (for exclusion)

    // TODO Run compilation & QA checks on Clojure code









    private static final Dependency eftestDependency = MavenDependencyFns.newDependency(
            "eftest",
            "eftest",
            "0.5.9"
    );


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.initialize();

        if (skip) {
            getLog().info(
                    i18nContext.getText(
                            "vivid.clojure-maven-plugin.action.skipping-execution-via-configuration",
                            CLOJURE_TEST_SKIP_PROPERTY_KEY
                    )
            );
            return;
        }

        final Map<String, String> options = HashMap.of(
                "junit-report-filename", mavenSession.getCurrentProject().getBuild().getDirectory() + "/clojure-test-reports/all-tests.xml"
        );

        try {
            final List<File> x = List
                    .of(new File(System.getProperty("user.dir"), "src/test/clojure")) // TODO
                    .appendAll(MavenDependencyFns.resolveToFiles(this, eftestDependency))
                    ;
            ClassPathology.addToClassLoader(this, x);
        } catch (final Exception e) {
            throw new MojoExecutionException(
                    "classloader",
                    e
            );
        }
        final Either<Message, Object> res = clojureDotTestRunner(this, options);
        if (res.isLeft()) {
            throw new MojoFailureException(
                    "problemo",
                    res.getLeft().getCause().get()
            );
        }

        final Object r = res.get();
        if (!(r instanceof Boolean)) {
            throw new MojoExecutionException(
                    VCMPE1InternalError.message("Unexpected result type returned from Clojure")
                            .render(this)
            );
        }
        final boolean allTestsPassed = (Boolean) r;
        if (!allTestsPassed) {
            throw new MojoFailureException("There are test failures.\n\n" +
                    "Please refer to " + options.getOrElse("junit-report-filename", "the build output directory") + " for the individual test results.");
        }


        //     While in Clojure-land, output to getLog()
        //     Report results back to Java mojo
        // Write test summary
        // Output the JUnit report file where Maven and CI tools can expect it
        // Return back to Maven, possibly a test failure or error
    }

    private static Either<Message, Object> clojureDotTestRunner(
            final AbstractCMPMojo mojo,
            final Map<String, String> eftestOptions
    ) {
        try {
            RT.loadResourceScript("vivid/cmp/clojure_dot_test_runner.clj");

            Clojure.var("clojure.core", "require")
                    .invoke(Symbol.intern("vivid.cmp.clojure-dot-test-runner"));
            final Object response =
                    Clojure.var("vivid.cmp.clojure-dot-test-runner", "run-tests")
                            .invoke(
                                    mojo,
                                    eftestOptions.toJavaMap()
                            );

            return Either.right(response);
        } catch (final IOException e) {
            return Either.left(VCMPE1InternalError.message(
                    "Could not run test runner",
                    e
            ));
        }
    }

}
