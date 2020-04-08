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
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import vivid.cmp.fns.ClassPathology;
import vivid.cmp.fns.MavenDependencyFns;
import vivid.polypara.annotation.Constant;

import java.io.IOException;

/**
 * Run Leiningen tasks in-process within Maven.
 *
 * This is faster and less resource-intensive than spawning a sub-process for a fresh
 * JVM to start Leiningen.
 *
 * Specify the Leiningen version with the {@code version} parameter, and this goal will
 * resolve (download) that version of Leiningen and its dependencies.
 *
 * Set the {@code args} parameter the same as your would from the command line.
 * Tasks, aliases, "do", any valid Lein arguments are fair game.
 * Be mindful of what you instruct Leiningen to do. For example, "clean"ing the project
 * may suit your purpose, or may lead to unintended consequences for the remainder of
 * the Maven build.
 *
 * Dirty details: Leiningen is run directly within Maven's JVM process, mostly
 * mitigating the time and space penalties of starting Lein in a new sub-process.
 * Measures are taken to intercept Lein (System/exit) behavior for the purpose of
 * returning orderly control back to Maven.
 * A primary trade-off of this goal's design is that a catastrophic process-killing
 * event in Leiningen will abruptly terminate Maven with it. If this matters to you,
 * an alternative is to run Leiningen in a sub-process i.e. via
 * org.codehaus.mojo:exec-maven-plugin, and incur the associated time- and resource-
 * consumption penalties.
 *
 * @since 0.1.0
 */
@Mojo(
        name = AbstractCMPMojo.LEININGEN_GOAL_NAME
)
public class LeiningenMojo extends AbstractCMPMojo {

    private static final String LEININGEN_LIB_DEPENDENCY_MAVEN_G_A = "leiningen";

    private static final String VIVID_CLOJURE_MAVEN_PLUGIN_NS = "vivid.cmp.leiningen";
    private static final String LEIN_MAIN_FN = "lein-main";


    @Constant
    private static final String CLOJURE_GOAL_PROPERTY_KEY_PREFIX =
            CLOJURE_MAVEN_PLUGIN_ID + "." + LEININGEN_GOAL_NAME + ".";

    @Constant
    private static final String LEININGEN_GOAL_ARGS_PROPERTY_KEY =
            CLOJURE_GOAL_PROPERTY_KEY_PREFIX + "args";

    @Constant
    private static final String LEININGEN_GOAL_VERSION_PROPERTY_KEY =
            CLOJURE_GOAL_PROPERTY_KEY_PREFIX + "version";


    //
    // User-provided configuration
    //

    @Parameter(required = true, property = LEININGEN_GOAL_ARGS_PROPERTY_KEY)
    private String args;

    @Parameter(required = true, property = LEININGEN_GOAL_VERSION_PROPERTY_KEY)
    private String version;


    @Override
    public void execute()
            throws MojoExecutionException, MojoFailureException {
        super.initialize();

        final Dependency dependency = MavenDependencyFns.newDependency(
                LEININGEN_LIB_DEPENDENCY_MAVEN_G_A,
                LEININGEN_LIB_DEPENDENCY_MAVEN_G_A,
                version
        );

        try {
            ClassPathology.addToClassLoader(
                    this,
                    MavenDependencyFns.resolveArtifact(this, dependency),
                    MavenDependencyFns.resolveDependencies(this, dependency)
            );
            lein(args);
        } catch (final MojoFailureException e) {
            throw e;
        } catch (final Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    private static void lein(
            final String args
    ) throws IOException {
        RT.loadResourceScript("vivid/cmp/leiningen.clj");

        // Clojure code ..
        //
        // (clojure.core/require vivid.cmp.leiningen)
        // (vivid.cmp.leiningen/lein-main task)

        // .. in its Java equivalent:
        //
        Clojure.var("clojure.core", "require")
                .invoke(Symbol.intern(VIVID_CLOJURE_MAVEN_PLUGIN_NS));
        Clojure.var(VIVID_CLOJURE_MAVEN_PLUGIN_NS, LEIN_MAIN_FN)
                .invoke(args);
    }

}
