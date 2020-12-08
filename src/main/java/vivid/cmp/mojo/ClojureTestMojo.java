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
import io.vavr.control.Option;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import vivid.cmp.datatypes.ClasspathScope;
import vivid.cmp.datatypes.ClojureMojoState;
import vivid.cmp.fns.ClassPathology;
import vivid.cmp.fns.MavenDependencyFns;
import vivid.cmp.fns.MojoFns;
import vivid.cmp.messages.Message;
import vivid.cmp.messages.VCMPE1InternalError;
import vivid.cmp.messages.VCMPE3ItemNotFound;
import vivid.cmp.messages.VCMPE4IndeterminateExecutionId;
import vivid.polypara.annotation.Constant;

import java.io.File;
import java.io.IOException;

/**
 * Runs clojure.test tests and reports results with JUnit-compatible output,
 * similar to maven-surefire-plugin.
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
    private static final String CLOJURE_TEST_CLOJUREGOALEXECUTIONID_PARAMETER_KEY = "clojureGoalExecutionId";
    @Constant
    private static final String CLOJURE_TEST_CLOJUREGOALEXECUTIONID_PROPERTY_KEY =
            CLOJURE_TEST_GOAL_PROPERTY_KEY_PREFIX + CLOJURE_TEST_CLOJUREGOALEXECUTIONID_PARAMETER_KEY;

    @Constant
    private static final String CLOJURE_TEST_MULTITHREAD_PARAMETER_KEY = "multithread";
    @Constant
    private static final String CLOJURE_TEST_MULTITHREAD_PROPERTY_KEY =
            CLOJURE_TEST_GOAL_PROPERTY_KEY_PREFIX + CLOJURE_TEST_MULTITHREAD_PARAMETER_KEY;

    @Constant
    private static final String CLOJURE_TEST_SKIP_PARAMETER_KEY = "skip";
    @Constant
    private static final String CLOJURE_TEST_SKIP_PROPERTY_KEY =
            CLOJURE_TEST_GOAL_PROPERTY_KEY_PREFIX + CLOJURE_TEST_SKIP_PARAMETER_KEY;

    @Constant
    private static final String CLOJURE_TEST_TESTFAILUREIGNORE_PARAMETER_KEY = "testFailureIgnore";
    @Constant
    private static final String CLOJURE_TEST_TESTFAILUREIGNORE_PROPERTY_KEY =
            CLOJURE_TEST_GOAL_PROPERTY_KEY_PREFIX + CLOJURE_TEST_TESTFAILUREIGNORE_PARAMETER_KEY;


    //
    // User-provided configuration
    //

    /**
     * Runs tests with the dependency and path settings of a specific {@code clojure}
     * goal, specified by its execution ID.
     *
     * @since 0.3.0
     */
    @Parameter(property = CLOJURE_TEST_CLOJUREGOALEXECUTIONID_PROPERTY_KEY)
    private String clojureGoalExecutionId;

    /**
     * Controls eftest's multithread setting. eftest by default runs tests in parallel,
     * but this goal mimics maven-surefire-plugin's default setting of serial execution.
     * Set to {@code true} to run your tests in parallel.
     *
     * @since 0.3.0
     */
    @Parameter(defaultValue = "false", property = CLOJURE_TEST_MULTITHREAD_PROPERTY_KEY)
    private boolean multithread;

    /**
     * The test run will be skipped when {@code true}.
     *
     * @since 0.3.0
     */
    @Parameter(defaultValue = "${maven.test.skip}", property = CLOJURE_TEST_SKIP_PROPERTY_KEY)
    private boolean skip;

    /**
     * When {@code true}, doesn't report failure back to Maven when not all tests pass.
     *
     * @since 0.3.0
     */
    @Parameter(
            defaultValue = "${maven.test.failure.ignore}",
            property = CLOJURE_TEST_TESTFAILUREIGNORE_PROPERTY_KEY
    )
    private boolean testFailureIgnore;


    private static final Dependency eftestDependency = MavenDependencyFns.newDependency(
            "eftest",
            "eftest",
            "0.5.9"
    );


    /**
     * Uses a 'clojure' goal execution ID if specified, uses the default if none are defined,
     * use the singular configuration if defined, otherwise gives up.
     */
    private static Either<Message, ClojureMojoState> selectClojureMojoConfig(
            final Map<String, ClojureMojoState> configs,
            final String executionId
    ) {
        if (executionId != null) {
            final Option<ClojureMojoState> a = configs.get(executionId);
            return a.toEither(VCMPE3ItemNotFound.message(
                    String.format(
                            "'%s' goal execution ID '%s'",
                            AbstractCMPMojo.CLOJURE_MOJO_GOAL_NAME,
                            executionId
                    )
            ));
        }
        if (configs.isEmpty()) {
            return Either.right(ClojureMojoState.DEFAULT_STATE);
        }
        if (configs.size() == 1) {
            return Either.right(configs.get()._2);
        }
        // At this point, configs.size() >= 2
        return Either.left(VCMPE4IndeterminateExecutionId.message(
                CLOJURE_TEST_CLOJUREGOALEXECUTIONID_PARAMETER_KEY,
                AbstractCMPMojo.CLOJURE_TEST_MOJO_GOAL_NAME,
                AbstractCMPMojo.CLOJURE_MOJO_GOAL_NAME,
                configs.keySet()
        ));
    }

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

        final Either<Message, ClojureMojoState> selectedConfig = MojoFns.myPluginExecutionConfigurations(
                this,
                pluginDescriptor.getPluginLookupKey(),
                AbstractCMPMojo.CLOJURE_MOJO_GOAL_NAME
        )
                .flatMap(configs -> selectClojureMojoConfig(configs, clojureGoalExecutionId));
        if (selectedConfig.isLeft()) {
            throw new MojoFailureException(
                    selectedConfig.getLeft().render(this)
            );
        }
        final ClojureMojoState clojureMojoState = selectedConfig.get()
                .setClasspathScope(ClasspathScope.TEST);

        final Map<String, Object> testRunnerOptions = HashMap.of(
                "junit-report-filename", mavenSession.getCurrentProject().getBuild().getDirectory() + "/clojure-test-reports/all-tests.xml",
                CLOJURE_TEST_MULTITHREAD_PARAMETER_KEY, multithread
        );

        try {
            ClassPathology.addToClassLoader(
                    this,
                    List
                            .ofAll(ClassPathology.getClassPathForScope(
                                    this,
                                    clojureMojoState,
                                    true,
                                    ClassPathology.PathStyle.ABSOLUTE
                            ))
                            .map(File::new)
                            .appendAll(MavenDependencyFns.resolveToFiles(this, eftestDependency))
            );
        } catch (final Exception e) {
            throw new MojoExecutionException(
                    VCMPE1InternalError.message("Unexpected exception while adding to classloader path").render(this),
                    e
            );
        }

        final Either<Message, Object> res = clojureDotTestRunner(this, testRunnerOptions);
        translateTestResultToMaven(this, res, testFailureIgnore, testRunnerOptions);
    }

    private static Either<Message, Object> clojureDotTestRunner(
            final AbstractCMPMojo mojo,
            final Map<String, Object> eftestOptions
    ) {
        mojo.getLog().debug(
                String.format(
                        "Running Clojure tests with options: %s",
                        eftestOptions
                )
        );
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

    private static void translateTestResultToMaven(
            final ClojureTestMojo mojo,
            final Either<Message, Object> testResult,
            final boolean testFailureIgnore,
            final Map<String, Object> testRunnerOptions
    ) throws MojoExecutionException, MojoFailureException {
        if (testResult.isLeft()) {
            throw new MojoFailureException(
                    testResult.getLeft().render(mojo),
                    testResult.getLeft().getCause().get()
            );
        }

        final Object r = testResult.get();
        if (!(r instanceof Boolean)) {
            throw new MojoExecutionException(
                    VCMPE1InternalError.message("Unexpected result type returned from vivid/cmp/clojure_dot_test_runner.clj")
                            .render(mojo)
            );
        }

        final boolean allTestsPassed = (Boolean) r;
        if (!allTestsPassed) {
            if (testFailureIgnore) {
                mojo.getLog().debug(CLOJURE_TEST_TESTFAILUREIGNORE_PROPERTY_KEY + " = true; ignoring negative test result");
            } else {
                throw new MojoFailureException(
                        mojo.i18nContext.getText(
                                "vivid.clojure-maven-plugin.action.test-failures",
                                testRunnerOptions.getOrElse("junit-report-filename", "the build output directory")
                        )
                );
            }
        }
    }

}
