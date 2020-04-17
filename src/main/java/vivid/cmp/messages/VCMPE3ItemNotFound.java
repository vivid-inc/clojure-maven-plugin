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

import vivid.cmp.mojo.AbstractCMPMojo;

/**
 * @since 0.3.0
 */
public class VCMPE3ItemNotFound implements Message {

    private static final String I18N_KEY = "vivid.clojure-maven-plugin.error.vcmpe-3-item-not-found";

    private final String item;

    private VCMPE3ItemNotFound(
            final String item
    ) {
        this.item = item;
    }

    public static Message message(
            final String item
    ) {
        return new VCMPE3ItemNotFound(
                item
        );
    }

    @Override
    public String render(
            AbstractCMPMojo mojo
    ) {
        return mojo.i18nContext().getText(
                I18N_KEY,
                item
        );
    }

}
