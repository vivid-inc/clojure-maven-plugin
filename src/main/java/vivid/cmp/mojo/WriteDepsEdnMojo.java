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

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import us.bpsm.edn.printer.Printers;
import vivid.cmp.classpath.ClasspathScopes;
import vivid.cmp.components.DepsEdnDataStructure;
import vivid.cmp.components.Resolution;
import vivid.cmp.components.Static;
import vivid.cmp.messages.SneakyMojoException;
import vivid.cmp.messages.VCMPE1InternalError;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.function.Predicate;


/**
 * Writes a 'deps.edn' project reflecting current pom.xml settings.
 *
 * @since 0.1.0
 */
@Mojo(
        name = AbstractCMPMojo.POM_CMP_WRITE_DEPS_EDN_MOJO_GOAL_NAME
)
public class WriteDepsEdnMojo extends AbstractCMPMojo {


    //
    // Maven components
    //

    @Parameter(defaultValue = "${plugin}", readonly = true, required = true)
    private PluginDescriptor pluginDescriptor;



    //
    // User-provided configuration
    //

    @Parameter(defaultValue = "deps.edn")
    private String filename;


    @Override
    public void execute()
            throws MojoExecutionException, MojoFailureException {
        super.initialize();

        try {
            // TODO output one alias per configuration
            final io.vavr.collection.Map<String, ClojureMojoState> states = myPluginExecutionConfigurations(this);
            final Object ednValue = DepsEdnDataStructure.create(
                    paths(this, states.get()._2),
                    Resolution.getResolvedDependencies(
                            this,
                            states.get()._2
                    )
            );

            final String deps_edn = Printers.printString(
                    Printers.prettyPrinterProtocol(),
                    ednValue
            );

            Static.writeFile(
                    Paths.get(filename),
                    StandardCharsets.UTF_8,
                    deps_edn + '\n'
            );

            getLog().info(
                    i18nContext.getText(
                            "vivid.clojure-maven-plugin.action.wrote-deps-edn",
                            filename
                    )
            );
        } catch (final SneakyMojoException ex) {
            throw SneakyMojoException.unwrap(ex);
        } catch (final Exception ex) {
            throw VCMPE1InternalError.asNewMojoExecutionException(
                    this,
                    "Unexpected exception",
                    ex
            );
        }
    }

    private io.vavr.collection.Map<String, ClojureMojoState> myPluginExecutionConfigurations(
            final AbstractCMPMojo mojo
    ) {
        final Plugin myself = mavenSession.getCurrentProject().getPlugin(
                pluginDescriptor.getPluginLookupKey()
        );

        final Predicate<PluginExecution> hasClojureGoal = e ->
                List.ofAll(e.getGoals())
                        .find(AbstractCMPMojo.POM_CMP_CLOJURE_MOJO_GOAL_NAME::equalsIgnoreCase)
                        .isDefined();

        return
                Stream.ofAll(myself.getExecutions())
                        .filter(hasClojureGoal)
                        .toMap(PluginExecution::getId, x -> asClojureMojoState(mojo, x));
    }

    private static Function1<Xpp3Dom, List<String>> getChildrenValues =
            dom -> Stream.of(dom.getChildren())
                    .map(Xpp3Dom::getValue)
                    .toList();
    private static Function2<ClojureMojoState, Xpp3Dom, ClojureMojoState> updateInPlaceClojureMojoState =
            (state, dom) -> {
                final String key = dom.getName();
                switch(key) {
                    case "args":           return state.setArgs(Option.of(dom.getValue()));
                    case "executable":     return state.setExecutable(dom.getValue());
                    case "classpathScope": return state.setClasspathScope(ClasspathScopes.valueOf(dom.getValue()));
                    case "sourcePaths":    return state.setSourcePaths(getChildrenValues.apply(dom));
                    case "testPaths":      return state.setTestPaths(getChildrenValues.apply(dom));
                    default:
                        throw new SneakyMojoException(
                                VCMPE1InternalError.asNewMojoExecutionException(
                                        null, // TODO
                                        "Unknown " + ClojureMojoState.class.getName() + " field name: " + key
                                )
                        );
                }
            };

    private static ClojureMojoState asClojureMojoState(
            final AbstractCMPMojo mojo,
            final PluginExecution pluginExecution
    ) {
        final Object untyped = pluginExecution.getConfiguration();
        if (!(untyped instanceof Xpp3Dom)) {
            throw new SneakyMojoException(
                    VCMPE1InternalError.asNewMojoExecutionException(
                            mojo,
                            "pluginExecution.getConfiguration() returned an " +
                                    "object of an unknown type: " + untyped.getClass()
                    )
            );
        }

        return
                Stream.of(((Xpp3Dom) untyped).getChildren())
                        .foldLeft(
                                ClojureMojoState.DEFAULT_STATE,
                                updateInPlaceClojureMojoState
                        );
    }

    private static Stream<String> paths(
            final AbstractCMPMojo mojo,
            final ClojureMojoState state
    ) {
        return Stream
                .ofAll(state.sourcePaths)
                .append(
                        mojo.mavenSession().getCurrentProject().getBuild().getOutputDirectory()
                )
                .appendAll(
                        state.classpathScope == ClasspathScopes.TEST ?
                                Stream.ofAll(state.testPaths)
                                        .append(mojo.mavenSession().getCurrentProject().getBuild().getOutputDirectory())
                                : Stream.empty()
                );
    }

}
