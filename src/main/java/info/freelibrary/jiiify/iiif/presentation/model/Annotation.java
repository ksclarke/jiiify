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

import java.util.List;

import info.freelibrary.jiiify.iiif.presentation.model.other.Metadata;

/**
 * <p>
 * Recommended URI Pattern: {scheme}://{host}/{prefix}/{identifier}/annotation/{name}
 * </p>
 *
 * @author Ralf Eichinger
 */
@Deprecated
public class Annotation extends AbstractIiifResource {

    private String myDescription; // optional

    private String myLabel; // optional

    private List<Metadata> myMetadata; // optional

    private String myThumbnail; // optional

    private String myViewingHint; // optional

    /**
     * Creates IIIF annotation resource.
     */
    public Annotation() {
        myType = "oa:Annotation";
    }

    /**
     * Gets the annotation resource description.
     *
     * @return The annotation resource description
     */
    public String getDescription() {
        return myDescription;
    }

    /**
     * Sets the annotation resource description.
     *
     * @param aDescription The annotation resource description
     */
    public void setDescription(final String aDescription) {
        myDescription = aDescription;
    }

    /**
     * Gets the annotation resource label.
     *
     * @return The annotation resource label
     */
    public String getLabel() {
        return myLabel;
    }

    /**
     * Sets the annotation resource label.
     *
     * @param aLabel The annotation resource label
     */
    public void setLabel(final String aLabel) {
        myLabel = aLabel;
    }

    /**
     * Gets the annotation resource metadata.
     *
     * @return The annotation resource metadata
     */
    public List<Metadata> getMetadata() {
        return myMetadata;
    }

    /**
     * Sets the annotation resource metadata.
     *
     * @param aMetadata The annotation resource metadata
     */
    public void setMetadata(final List<Metadata> aMetadata) {
        myMetadata = aMetadata;
    }

    /**
     * Gets the annotation resource thumbnail.
     *
     * @return The annotation resource thumbnail
     */
    public String getThumbnail() {
        return myThumbnail;
    }

    /**
     * Sets the annotation resource thumbnail.
     *
     * @param aThumbnail The annotation resource thumbnail
     */
    public void setThumbnail(final String aThumbnail) {
        myThumbnail = aThumbnail;
    }

    /**
     * Gets the annotation resource viewing hint.
     *
     * @return The annotation resource viewing hint
     */
    public String getViewingHint() {
        return myViewingHint;
    }

    /**
     * Sets the annotation resource viewing hint.
     *
     * @param aViewingHint The annotation resource viewing hint
     */
    public void setViewingHint(final String aViewingHint) {
        myViewingHint = aViewingHint;
    }

}
