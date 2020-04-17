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

import io.vavr.control.Either;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import us.bpsm.edn.printer.Printers;
import vivid.cmp.datatypes.DepsEdn;
import vivid.cmp.fns.FileFns;
import vivid.cmp.fns.MojoFns;
import vivid.cmp.messages.Message;
import vivid.polypara.annotation.Constant;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Writes a 'deps.edn' file containing equivalents to the execution configuration of
 * each {@code clojure} goal in {@code pom.xml}.
 *
 * Each 'clojure' goal appears as an {@code :alias} in 'deps.edn', and can be run from
 * the CLI directly:
 *
 * <pre>
 *     $ clojure -A:ALIAS_NAME
 *</pre>
 *
 * Any existing 'deps.edn' file at specified path will be overwritten.
 * The pathname defaults to 'deps.edn' in the current directory.
 * If pathname indicates a directory, 'deps.edn' will be written within that directory.
 *
 * @since 0.2.0
 */
@Mojo(
        defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
        name = AbstractCMPMojo.DEPS_EDN_MOJO_GOAL_NAME
)
public class DepsEdnMojo extends AbstractCMPMojo {

    @Constant
    private static final String DEPS_EDN_GOAL_PROPERTY_KEY_PREFIX =
            CLOJURE_MAVEN_PLUGIN_ID + "." + DEPS_EDN_MOJO_GOAL_NAME + ".";

    @Constant
    private static final String DEPS_EDN_PATHNAME_PARAMETER_KEY = "pathname";
    @Constant
    private static final String DEPS_EDN_PATHNAME_PROPERTY_KEY =
            DEPS_EDN_GOAL_PROPERTY_KEY_PREFIX + DEPS_EDN_PATHNAME_PARAMETER_KEY;
    @Constant
    private static final String DEPS_EDN_PATHNAME_PROPERTY_DEFAULT_VALUE = "deps.edn";


    //
    // User-provided configuration
    //

    @Parameter(defaultValue = DEPS_EDN_PATHNAME_PROPERTY_DEFAULT_VALUE, property = DEPS_EDN_PATHNAME_PROPERTY_KEY)
    private String pathname;


    @Override
    public void execute()
            throws MojoExecutionException {
        super.initialize();

        // TODO Calculate the common :deps, :paths to reduce duplication in the :alias es

        final Either<Message, Object> result =
                MojoFns.myPluginExecutionConfigurations(
                        this,
                        pluginDescriptor.getPluginLookupKey(),
                        AbstractCMPMojo.CLOJURE_MOJO_GOAL_NAME
                )
                        .map(configs -> DepsEdn.create(this, configs))
                        .map(edn -> Printers.printString(
                                Printers.prettyPrinterProtocol(),
                                edn
                        ))
                        .map(depsEdn -> FileFns.writeFile(
                                depsEdnPath(pathname),
                                StandardCharsets.UTF_8,
                                depsEdn + '\n'));

        if (result.isRight()) {
            getLog().info(
                    i18nContext.getText(
                            "vivid.clojure-maven-plugin.action.wrote",
                            pathname
                    )
            );
        } else {
            throw new MojoExecutionException(
                    result.getLeft().render(this)
            );
        }
    }

    /**
     * {@param pathname} can specify any valid path to a {@code deps.edn} file.
     * If {@param pathname} points to a directory, this returns a path to the
     * default filename of "deps.edn" within the directory.
     */
    private static Path depsEdnPath(
            final String pathname
    ) {
        Path p = Paths.get(pathname);
        return p.toFile().isDirectory() ?
                p.resolve(DEPS_EDN_PATHNAME_PROPERTY_DEFAULT_VALUE) :
                p;
    }

}
