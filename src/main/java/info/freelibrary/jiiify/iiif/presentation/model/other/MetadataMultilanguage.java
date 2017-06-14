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

import java.util.List;

/**
 * @author Ralf Eichinger
 */
@Deprecated
public class MetadataMultilanguage extends Metadata {

    private final List<MetadataLocalizedValue> myValue;

    /**
     * Creates a multi-language metadata element.
     *
     * @param aLabel A metadata label
     * @param aValueList A list of metadata values
     */
    public MetadataMultilanguage(final String aLabel, final List<MetadataLocalizedValue> aValueList) {
        super(aLabel);
        myValue = aValueList;
    }

    /**
     * Gets the list of metadata values.
     *
     * @return The list of metadata values
     */
    public List<MetadataLocalizedValue> getValue() {
        return myValue;
    }

}
