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

/**
 * Intentionally wrap checked exceptions in unchecked exceptions for the
 * express purposes of:
 *
 * 1. Reducing exception-handling code within lambdas.
 * 2. Passing informative error messages in-situ to users.
 *
 * Unwrap the causative exception and handle it on the outside with familiar
 * try-catch semantics via {@code ex.getCause()}.
 */
@SuppressWarnings("serial")
public class SneakyMojoException extends RuntimeException {

    // TODO Consider using Either<Exception, ?> instead of sneaky exceptions...

    SneakyMojoException(final String message, final Throwable cause) { super(message, cause); }
    public SneakyMojoException(final Throwable cause) { super(cause.getMessage(), cause); }

    public static MojoExecutionException unwrap(
            final SneakyMojoException ex
    ) {
        // Unwrap the causative Throwable
        final Throwable cause = ex.getCause();
        return new MojoExecutionException(
                cause.getMessage(),
                cause
        );
    }

}
