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

import io.vavr.collection.Stream;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;
import java.util.Map;

public class SubProcess {

    private SubProcess() {
        // Hide the public constructor
    }

    static void executeSubProcess(
            final MojoComponents mojo,
            final String clojureExecutable,
            final String args,
            final Stream<String> classPath,
            final Map<String, String> env
    ) throws MojoExecutionException {
        // Employ Apache's commons-exec to handle the sub-process
        final Executor executor = new DefaultExecutor();

        // stdin will not be connected to this process, implying no interactivity
        executor.setStreamHandler(new PumpStreamHandler());

        // The sub-process' working directory is set to the basedir of the Maven project
        executor.setWorkingDirectory(mojo.mavenSession().getCurrentProject().getBasedir());

        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());

        final CommandLine commandLine = new CommandLine(clojureExecutable);

        // The Clojure sub-process utilizes Maven's classpath
        commandLine.addArgument("-Scp");
        commandLine.addArgument(
                Stream.ofAll(classPath).mkString(":")
        );

        if (args != null) {
            commandLine.addArguments(args);
        }

        SubProcess.exec(
                mojo,
                executor,
                commandLine,
                env
        );
    }

    private static void exec(
            final MojoComponents mojo,
            final Executor exec,
            final CommandLine commandLine,
            final Map<String, String> environment
    ) throws MojoExecutionException {
        mojo.log().debug(
                String.format(
                        "Command line: %s",
                        String.join(" ", commandLine.toStrings())
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
                    mojo.i18nContext().getText(
                            "vivid.clojure-maven-plugin.error.vcmpe-2-command-exit-value",
                            commandLine.getExecutable(),
                            exitValue
                    ),
                    failureException
            );
        }
    }
}
