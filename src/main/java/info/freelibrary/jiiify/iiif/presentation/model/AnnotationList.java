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
 * Recommended URI Pattern: {scheme}://{host}/{prefix}/{identifier}/list/{name}
 *
 * @author Ralf Eichinger
 */
@Deprecated
public class AnnotationList extends AbstractIiifResource {

    private String myDescription; // optional

    private String myLabel; // optional

    private List<Metadata> myMetadata; // optional

    private String myThumbnail; // optional

    private String myViewingHint; // optional

    /**
     * Creates a new annotation list with the supplied ID.
     *
     * @param aID The ID for the annotation list
     */
    public AnnotationList(final String aID) {
        assert aID != null;
        myID = aID;

        myType = "sc:AnnotationList";
    }

    /**
     * Gets description of the annotation list.
     *
     * @return The annotation list's description
     */
    public String getDescription() {
        return myDescription;
    }

    /**
     * Sets description for the annotation list.
     *
     * @param aDescription A description for the annotation list
     */
    public void setDescription(final String aDescription) {
        myDescription = aDescription;
    }

    /**
     * Gets label of the annotation list.
     *
     * @return The annotation list's label
     */
    public String getLabel() {
        return myLabel;
    }

    /**
     * Sets label for the annotation list.
     *
     * @param aLabel A label for the annotation list
     */
    public void setLabel(final String aLabel) {
        myLabel = aLabel;
    }

    /**
     * Gets metadata for the annotation list.
     *
     * @return The annotation list's metadata
     */
    public List<Metadata> getMetadata() {
        return myMetadata;
    }

    /**
     * Sets metadata for the annotation list.
     *
     * @param aMetadata A metadata list for the annotation list
     */
    public void setMetadata(final List<Metadata> aMetadata) {
        myMetadata = aMetadata;
    }

    /**
     * Gets a thumbnail for the annotation list.
     *
     * @return The annotation list's thumbnail
     */
    public String getThumbnail() {
        return myThumbnail;
    }

    /**
     * Sets the annotation list's thumbnail.
     *
     * @param aThumbnail A thumbnail for the annotation list
     */
    public void setThumbnail(final String aThumbnail) {
        myThumbnail = aThumbnail;
    }

    /**
     * Gets the annotation list's viewing hint.
     *
     * @return The annotation list's viewing hint
     */
    public String getViewingHint() {
        return myViewingHint;
    }

    /**
     * Sets the annotation list's viewing hint.
     *
     * @param aViewingHint A viewing hint for the annotation list
     */
    public void setViewingHint(final String aViewingHint) {
        myViewingHint = aViewingHint;
    }

}
