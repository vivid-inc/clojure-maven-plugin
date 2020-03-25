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

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.i18n.I18N;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Executes 'clojure' as a sub-process.
 *
 * @since 0.1.0
 */
@Mojo(
        name = Static.POM_CMP_CLOJURE_MOJO_NAME,
        requiresDependencyResolution = ResolutionScope.TEST
)
public class ClojureMojo extends AbstractMojo {

    private I18nContext i18nContext;


    //
    // Maven execution environment configuration
    //

    @Component
    private I18N i18n;

    @Parameter(required = true, property = "plugin.artifacts")
    private java.util.List<Artifact> pluginArtifacts;

    @Parameter(required = true, readonly = true, property = "session")
    private MavenSession session;

    @Parameter(required = true, readonly = true, property = "project.testClasspathElements")
    private List<String> testClasspathElements;


    //
    // User-provided configuration
    //

    @Parameter(property = Static.POM_CMP_ARGS_PROPERTY_KEY)
    private String args;

    @Parameter(property = Static.POM_CMP_CLOJURE_EXECUTABLE_PROPERTY_KEY, defaultValue = Static.POM_CMP_CLOJURE_EXECUTABLE_PROPERTY_KEY)
    private String clojureExecutable;

    public void execute()
        throws MojoExecutionException {

        i18nContext = new I18nContext(i18n);

// Referencing
// https://github.com/talios/clojure-maven-plugin
// https://github.com/cognitect-labs/test-runner/blob/master/deps.edn
// https://oli.me.uk/clojure-and-clojurescript-testing-with-the-clojure-cli/
// https://github.com/ingesolvoll/lein-maven-plugin
// https://github.com/redbadger/test-report-junit-xml

// Set the classpath to that of Maven.
// Run the clojure.test tests.
// Write JUnit report files.
// Report results on console and back to Maven ala surefire.

        executeSubProcess();
    }

    private void executeSubProcess() throws MojoExecutionException {
        // Employ Apache's commons-exec to handle the sub-process
        final Executor exec = new DefaultExecutor();

        // stdin will not be connected to this process, implying no interactivity
        exec.setStreamHandler(new PumpStreamHandler());

        // The sub-process' working directory is set to the basedir of the Maven project
        exec.setWorkingDirectory(projectBasedir());

        exec.setProcessDestroyer(new ShutdownHookProcessDestroyer());

        final CommandLine commandLine = new CommandLine(clojureExecutable);
        commandLine.addArguments(args);

        // The Clojure sub-process utilizes Maven's classpath
        commandLine.addArgument("-Scp");
        commandLine.addArgument(
                projectTestClassPathElements()
                        .collect(Collectors.joining(":"))
        );

        // The sub-process inherits our environment variables
        final Map<String, String> env = new HashMap<>(System.getenv());

        exec(exec, commandLine, env,
                getLog(), i18nContext);
    }

    private static void exec(
            final Executor exec,
            final CommandLine commandLine,
            final Map<String, String> environment,
            final Log log,
            final I18nContext i18nContext
    ) throws MojoExecutionException {
        log.debug(
                String.format(
                        "Command line: %s",
                        commandLine.toString()
                )
        );

        int exitValue;
        Exception failureException = null;
        try {
            exitValue = exec.execute(commandLine, environment);
        } catch (final ExecuteException e) {
            exitValue = e.getExitValue();
            failureException = e;
        } catch (final IOException e) {
            exitValue = 1;
            failureException = e;
        }

        if (exitValue != 0) {
            throw new MojoExecutionException(
                    i18nContext.getText(
                            "vivid.clojure-maven-plugin.error.vcmpe-2-command-exit-value",
                            commandLine.getExecutable(),
                            exitValue
                    ),
                    failureException
            );
        }
    }

    private Stream<String> projectTestClassPathElements() {
        return Stream.concat(
                pluginArtifacts.stream().map(a -> a.getFile().getPath()),
                testClasspathElements.stream()
        );
    }

    private File projectBasedir() {
        return session.getCurrentProject().getBasedir();
    }

}
