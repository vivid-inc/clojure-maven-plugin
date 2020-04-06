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

import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import io.vavr.collection.TreeMap;
import io.vavr.control.Either;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import us.bpsm.edn.printer.Printers;
import vivid.cherimoya.annotation.Constant;
import vivid.cmp.datatypes.ClojureMojoState;
import vivid.cmp.datatypes.DepsEdn;
import vivid.cmp.fns.ClojureMojoConfigurationFns;
import vivid.cmp.fns.FileFns;
import vivid.cmp.fns.MojoFns;
import vivid.cmp.messages.Message;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiFunction;

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
            AbstractCMPMojo.DEPS_EDN_MOJO_GOAL_NAME + ".";

    @Constant
    private static final String DEPS_EDN_PATHNAME_PARAMETER_KEY = "pathname";
    @Constant
    private static final String DEPS_EDN_PATHNAME_PROPERTY_KEY =
            DEPS_EDN_GOAL_PROPERTY_KEY_PREFIX + DEPS_EDN_PATHNAME_PARAMETER_KEY;
    @Constant
    private static final String DEPS_EDN_PATHNAME_PROPERTY_DEFAULT_VALUE = "deps.edn";


    //
    // Maven components
    //

    @Parameter(defaultValue = "${plugin}", readonly = true, required = true)
    private PluginDescriptor pluginDescriptor;



    //
    // User-provided configuration
    //

    @Parameter(property = DEPS_EDN_PATHNAME_PROPERTY_KEY)
    private String pathname = DEPS_EDN_PATHNAME_PROPERTY_DEFAULT_VALUE;


    @Override
    public void execute()
            throws MojoExecutionException {
        super.initialize();

        // TODO Calculate the common :deps, :paths to reduce duplication in the :alias es

        final Either<Message, Object> result =
                myPluginExecutionConfigurations()
                        .map(x -> DepsEdn.create(this, x))
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
                            "vivid.clojure-maven-plugin.action.wrote-deps-edn",
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
     * {@param pathname} can specify any valid path. In the case of a directory,
     * this appends the filename  "deps.edn" and returns a path to the file within
     * the directory.
     */
    private static Path depsEdnPath(
            final String pathname
    ) {
        Path p = Paths.get(pathname);
        return p.toFile().isDirectory() ?
                p.resolve(DEPS_EDN_PATHNAME_PROPERTY_DEFAULT_VALUE) :
                p;
    }

    private Either<Message, Map<String, ClojureMojoState>> myPluginExecutionConfigurations() {
        final Plugin myself = mavenSession.getCurrentProject().getPlugin(
                pluginDescriptor.getPluginLookupKey()
        );
        return
                Stream.ofAll(myself.getExecutions())
                        .filter(MojoFns.hasGoalOfName(AbstractCMPMojo.CLOJURE_MOJO_GOAL_NAME))
                        .foldLeft(
                                Either.right(TreeMap.empty()),
                                mappedPluginExecutionStateCombinator()
                        );
    }

    private static BiFunction<
            Either<Message, Map<String, ClojureMojoState>>,
            PluginExecution,
            Either<Message, Map<String, ClojureMojoState>>>
    mappedPluginExecutionStateCombinator() {
        return (m, pluginExecution) -> {
            if (m.isLeft()) {
                return m;
            }
            final Either<Message, ClojureMojoState> state =
                    ClojureMojoConfigurationFns.asClojureMojoState(pluginExecution);
            if (state.isLeft()) {
                return Either.left(state.getLeft());
            }
            return Either.right(
                    m.get().put(
                            pluginExecution.getId(),
                            state.get()
                    )
            );
        };
    }

}
