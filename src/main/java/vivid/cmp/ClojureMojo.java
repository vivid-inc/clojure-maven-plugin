package vivid.cmp;

// Referencing
// https://github.com/talios/clojure-maven-plugin
// https://github.com/cognitect-labs/test-runner/blob/master/deps.edn
// https://oli.me.uk/clojure-and-clojurescript-testing-with-the-clojure-cli/
// https://github.com/ingesolvoll/lein-maven-plugin
// https://github.com/redbadger/test-report-junit-xml

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.i18n.I18N;

/**
 * @since 0.1.0
 */
@Mojo( name = Static.POM_CMP_CLOJURE_MOJO_NAME )
public class ClojureMojo extends AbstractMojo {

    private I18nContext i18nContext;

    @Component
    private I18N i18n;

    public void execute()
        throws MojoExecutionException {

        i18nContext = new I18nContext(i18n);

        getLog().info(
                i18nContext.getText(
                        "vivid.clojure-maven-plugin.action.greeting"
                )
        );

    }

}
