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

package vivid.cmp.messages;

import org.apache.maven.plugin.MojoExecutionException;
import vivid.cmp.mojo.AbstractCMPMojo;

/**
 * @since 0.1.0
 */
public class VCMPE1InternalError {

    private VCMPE1InternalError() {
        // Hide the public constructor
    }

    private static final String MESSAGE_I18N_KEY = "vivid.clojure-maven-plugin.error.vcmpe-1-internal-error";

    public static MojoExecutionException asNewMojoExecutionException(
            final AbstractCMPMojo mojo,
            final String message
    ) {
        return new MojoExecutionException(
                mojo.i18nContext().getText(
                        MESSAGE_I18N_KEY,
                        message
                )
        );
    }

    public static MojoExecutionException asNewMojoExecutionException(
            final AbstractCMPMojo mojo,
            final String message,
            final Exception ex
    ) {
        return new MojoExecutionException(
                mojo.i18nContext().getText(
                        MESSAGE_I18N_KEY,
                        message
                ),
                ex
        );
    }

}
