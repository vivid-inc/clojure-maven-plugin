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

package vivid.cmp.fns;

import io.vavr.control.Either;
import vivid.cmp.messages.Message;
import vivid.cmp.messages.VCMPE1InternalError;
import vivid.cmp.mojo.AbstractCMPMojo;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileFns {

    private FileFns() {
        // Hide the public constructor
    }

    public static Either<Message, Void> writeFile(
            final Path path,
            final Charset charset,
            final String content
    ) {
        try (final BufferedWriter writer = Files.newBufferedWriter(
                path,
                charset
        )) {
            writer.write(content);
            return Either.right(null);
        } catch (final IOException e) {
            return Either.left(
                    VCMPE1InternalError.message(
                            "Could not write file: " + path,
                            e
                    )
            );
        }
    }

    public static class UserDirSystemPropertyOverride implements Closeable {

        private static final String USER_DIR_SYSTEM_PROPERTY_KEY = "user.dir";

        private final String priorValue;
        private final AbstractCMPMojo mojo;

        public UserDirSystemPropertyOverride(
                final AbstractCMPMojo mojo,
                final String cwd
        ) {
            this.mojo = mojo;

            this.priorValue = System.getProperty(USER_DIR_SYSTEM_PROPERTY_KEY);

            System.setProperty(USER_DIR_SYSTEM_PROPERTY_KEY, cwd);
            mojo.getLog().debug(
                    String.format(
                            "Set system property %s to a new cwd: %s",
                            USER_DIR_SYSTEM_PROPERTY_KEY, cwd
                    )
            );
        }

        @Override
        public void close() {
            System.setProperty(USER_DIR_SYSTEM_PROPERTY_KEY, priorValue);
            mojo.getLog().debug(
                    String.format(
                            "Restored system property %s to its prior value: %s",
                            USER_DIR_SYSTEM_PROPERTY_KEY, priorValue
                    )
            );
        }

    }

}
