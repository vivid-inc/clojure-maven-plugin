package vivid.cmp;

// Referencing
// https://github.com/talios/clojure-maven-plugin
// https://github.com/cognitect-labs/test-runner/blob/master/deps.edn
// https://oli.me.uk/clojure-and-clojurescript-testing-with-the-clojure-cli/
// https://github.com/ingesolvoll/lein-maven-plugin
// https://github.com/redbadger/test-report-junit-xml

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

import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.i18n.I18N;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since 0.1.0
 */
@Mojo( name = Static.POM_CMP_CLOJURE_MOJO_NAME )
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


    public void execute()
        throws MojoExecutionException {

        i18nContext = new I18nContext(i18n);

        executeSubProcess(
                "clojure",
                args
        );

        // Set the classpath to that of Maven.
        // Run the clojure.test tests.
        // Write JUnit report files.
        // Report results on console and back to Maven ala surefire.

    }

    private void executeSubProcess(
            final String executable,
            final String arguments
    ) throws MojoExecutionException {
        // Employ Apache's commons-exec to handle the sub-process
        final Executor exec = new DefaultExecutor();

        // stdin will not be connected to this process, implying no interactivity.
        exec.setStreamHandler(new PumpStreamHandler());

        // The sub-process' working directory is set to the basedir of the Maven project.
        exec.setWorkingDirectory(projectBasedir());

        exec.setProcessDestroyer(new ShutdownHookProcessDestroyer());

        final CommandLine commandLine = new CommandLine(executable);
        commandLine.addArguments(arguments);

        // The sub-process inherits our environment variables.
        final Map<String, String> env = new HashMap<>(System.getenv());

        getLog().debug(
                String.format(
                        "Command line: %s %s",
                        executable,
                        commandLine.toString()
                )
        );

        int exitValue;
        Exception failureException = null;
        try {
            exitValue = exec.execute(commandLine, env);
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
                            executable,
                            exitValue
                    ),
                    failureException
            );
        }
    }

    private List<String> getRunWithClasspathElements() {
        final Set<String> classPathElements = new HashSet<>();
        //if (includePluginDependencies) {
            for (final Artifact artifact : pluginArtifacts) {
                classPathElements.add(artifact.getFile().getPath());
            }
        //}
        classPathElements.addAll(//runWithTests ?
                testClasspathElements //: classpathElements
        );

        return new ArrayList<>(classPathElements);
    }

    private File projectBasedir() {
        return session.getCurrentProject().getBasedir();
    }


}
