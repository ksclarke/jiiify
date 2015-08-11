/*
 * Copyright 2015 Ralf Eichinger
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

package info.freelibrary.jiiify.iiif.presentation.model;

/**
 * <p>
 * Recommended URI pattern: {scheme}://{host}/{prefix}/{identifier}/list/{name}</p>
 *
 * @author Ralf Eichinger
 */
public class OtherContent extends Content {

    private String label; // optional

    public OtherContent(final String id) {
        super(id);
        this.type = "oa:Annotation";
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }
}
