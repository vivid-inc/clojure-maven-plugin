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

package vivid.cmp;

import org.codehaus.plexus.i18n.I18N;

import java.util.Locale;

class I18nContext {

    private static final String I18N_RESOURCE_BUNDLE = "vivid-clojure-maven-plugin-i18n";

    private final I18N i18n;

    I18nContext(
            final I18N i18n
    ) {
        this.i18n = i18n;
    }

    String getText(
            final String i18nKey,
            final Object... args
    ) {
        final Locale locale = Locale.getDefault();
        return i18n.format(I18N_RESOURCE_BUNDLE, locale, i18nKey, args);
    }

}
