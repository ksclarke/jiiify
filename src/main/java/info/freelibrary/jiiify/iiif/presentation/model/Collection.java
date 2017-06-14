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
 * Recommended URI Pattern: {scheme}://{host}/{prefix}/collection/{name}
 *
 * @author Ralf Eichinger
 */
@Deprecated
public class Collection extends AbstractIiifResource {

    private String myDescription; // recommended
    private final String myLabel; // required
    private final List<Metadata> myMetadata; // recommended
    private String myThumbnail; // recommended
    private String myViewingHint; // optional

    /**
     * Creates IIIF collection with ID, label, and metadata.
     *
     * @param aID A collection ID
     * @param aLabel A collection label
     * @param aMetadata A metadata
     */
    public Collection(final String aID, final String aLabel, final List<Metadata> aMetadata) {
        assert aID != null;
        assert aLabel != null;

        myID = aID;
        myLabel = aLabel;
        myMetadata = aMetadata;
        myType = "sc:Collection";
    }

    /**
     * Gets the collection's description.
     *
     * @return The collection's description
     */
    public String getDescription() {
        return myDescription;
    }

    /**
     * Sets the collection's description.
     *
     * @param aDescription A description for collection
     */
    public void setDescription(final String aDescription) {
        myDescription = aDescription;
    }

    /**
     * Gets the collection label.
     *
     * @return The collection label
     */
    public String getLabel() {
        return myLabel;
    }

    /**
     * Gets the collection metadata
     *
     * @return The collection's metadata
     */
    public List<Metadata> getMetadata() {
        return myMetadata;
    }

    /**
     * Gets the collection thumbnail.
     *
     * @return The collection thumbnail
     */
    public String getThumbnail() {
        return myThumbnail;
    }

    /**
     * Sets the collection thumbnail.
     *
     * @param aThumbnail A thumbnail for the collection
     */
    public void setThumbnail(final String aThumbnail) {
        myThumbnail = aThumbnail;
    }

    /**
     * Gets the collection's viewing hint.
     *
     * @return The collection's viewing hint
     */
    public String getViewingHint() {
        return myViewingHint;
    }

    /**
     * Sets the collection's viewing hint.
     *
     * @param aViewingHint A viewing hint for the collection
     */
    public void setViewingHint(final String aViewingHint) {
        myViewingHint = aViewingHint;
    }

}
