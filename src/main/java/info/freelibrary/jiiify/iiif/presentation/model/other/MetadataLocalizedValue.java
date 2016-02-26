/*
 * Copyright 2015 Ralf Eichinger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.freelibrary.jiiify.iiif.presentation.model.other;

import java.util.Locale;

/**
 * @author Ralf Eichinger
 */
public class MetadataLocalizedValue {

    private final String myValue;

    private final String myLanguage;

    /**
     * Creates a localized metadata value.
     *
     * @param aValue A metadata value
     * @param aLocale A metadata locale
     */
    public MetadataLocalizedValue(final String aValue, final Locale aLocale) {
        myValue = aValue;
        myLanguage = aLocale.getLanguage();
    }

    /**
     * Gets the localized value.
     *
     * @return The localized value
     */
    public String getValue() {
        return myValue;
    }

    /**
     * Gets the value's language
     *
     * @return The value's language
     */
    public String getLanguage() {
        return myLanguage;
    }

}
