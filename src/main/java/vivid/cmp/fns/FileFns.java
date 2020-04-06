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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The context within which these Maven goals are configured is Maven,
 * identifiers use the CamelCase convention prevalent in Java and Maven
 * rather than dashed convention epitomized by Lisp.
 */
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

}
