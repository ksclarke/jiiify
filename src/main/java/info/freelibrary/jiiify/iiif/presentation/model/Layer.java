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
 * Recommended URI Pattern: {scheme}://{host}/{prefix}/{identifier}/layer/{name}
 *
 * @author Ralf Eichinger
 */
@Deprecated
public class Layer extends AbstractIiifResource {

    private String myDescription; // optional

    private final String myLabel; // required

    private List<Metadata> myMetadata; // optional

    private String myThumbnail; // optional

    private String myViewingDirection; // optional

    private String myViewingHint; // optional

    /**
     * Creates a IIIF layer.
     *
     * @param id A layer ID
     * @param label A layer label
     */
    public Layer(final String id, final String label) {
        assert id != null;
        assert label != null;

        myID = id;
        myLabel = label;

        myType = "sc:Layer";
    }

    /**
     * Gets the layer description.
     *
     * @return The layer description
     */
    public String getDescription() {
        return myDescription;
    }

    /**
     * Sets the layer description
     *
     * @param aDescription The layer description
     */
    public void setDescription(final String aDescription) {
        myDescription = aDescription;
    }

    /**
     * Gets the layer label.
     *
     * @return The layer label
     */
    public String getLabel() {
        return myLabel;
    }

    /**
     * Gets the layer metadata.
     *
     * @return The layer metadata
     */
    public List<Metadata> getMetadata() {
        return myMetadata;
    }

    /**
     * Sets the layer metadata.
     *
     * @param aMetadata The layer metadata
     */
    public void setMetadata(final List<Metadata> aMetadata) {
        myMetadata = aMetadata;
    }

    /**
     * Gets the layer thumbnail.
     *
     * @return The layer thumbnail
     */
    public String getThumbnail() {
        return myThumbnail;
    }

    /**
     * Sets the layer thumbnail.
     *
     * @param aThumbnail The layer thumbnail
     */
    public void setThumbnail(final String aThumbnail) {
        myThumbnail = aThumbnail;
    }

    /**
     * Gets the viewing direction.
     *
     * @return The viewing direction
     */
    public String getViewingDirection() {
        return myViewingDirection;
    }

    /**
     * Sets the layer's viewing direction.
     *
     * @param aViewingDirection The direction that canvases of the resource should be presented when rendered for the
     *        user to navigate and/or read. A range or layer may have a viewing direction.
     */
    public void setViewingDirection(final String aViewingDirection) {
        myViewingDirection = aViewingDirection;
    }

    /**
     * Gets the viewing direction
     *
     * @return The viewing direction
     */
    public String getViewingHint() {
        return myViewingHint;
    }

    /**
     * Sets the layer viewing hint.
     *
     * @param aViewingHint The layer viewing hint
     */
    public void setViewingHint(final String aViewingHint) {
        myViewingHint = aViewingHint;
    }

}
