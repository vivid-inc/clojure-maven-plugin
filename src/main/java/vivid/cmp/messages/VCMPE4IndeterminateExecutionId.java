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

import io.vavr.collection.Set;
import vivid.cmp.mojo.AbstractCMPMojo;

/**
 * @since 0.3.0
 */
public class VCMPE4IndeterminateExecutionId implements Message {

    private static final String I18N_KEY = "vivid.clojure-maven-plugin.error.vcmpe-4-indeterminate-execution-id";

    private final String property;
    private final String goal;
    private final String targetGoal;
    private final Set<String> executionIds;

    private VCMPE4IndeterminateExecutionId(
            final String property,
            final String goal,
            final String targetGoal,
            final Set<String> executionIds
    ) {
        this.property = property;
        this.goal = goal;
        this.targetGoal = targetGoal;
        this.executionIds = executionIds;
    }

    public static Message message(
            final String property,
            final String goal,
            final String targetGoal,
            final Set<String> executionIds
    ) {
        return new VCMPE4IndeterminateExecutionId(
                property,
                goal,
                targetGoal,
                executionIds
        );
    }

    @Override
    public String render(
            AbstractCMPMojo mojo
    ) {
        return mojo.i18nContext().getText(
                I18N_KEY,
                property,
                goal,
                targetGoal,
                String.join(", ", executionIds)
        );
    }

}
