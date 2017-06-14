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
 * Recommended URI Pattern: {scheme}://{host}/{prefix}/{identifier}/range/{name}
 *
 * @author Ralf Eichinger
 */
@Deprecated
public class Range extends AbstractIiifResource {

    private String myDescription; // optional

    private final String myLabel; // required

    private List<Metadata> myMetadata; // optional

    private String myStartCanvas; // optional

    private String myThumbnail; // optional

    private String myViewingDirection; // optional

    private String myViewingHint; // optional

    /**
     * Creates a IIIF range with an ID and label.
     *
     * @param aID A range resource ID
     * @param aLabel A range resource label
     */
    public Range(final String aID, final String aLabel) {
        assert aID != null;
        assert aLabel != null;

        myID = aID;
        myLabel = aLabel;
        myType = "sc:Range";
    }

    /**
     * Gets a range resource description.
     *
     * @return The range resource description
     */
    public String getDescription() {
        return myDescription;
    }

    /**
     * Sets the range resource's description.
     *
     * @param aDescription A description for the range resource
     */
    public void setDescription(final String aDescription) {
        myDescription = aDescription;
    }

    /**
     * Gets the range resource's label.
     *
     * @return The range resource's label
     */
    public String getLabel() {
        return myLabel;
    }

    /**
     * Gets the range resource's metadata.
     *
     * @return The range resource's metadata
     */
    public List<Metadata> getMetadata() {
        return myMetadata;
    }

    /**
     * Sets the range resource's metadata.
     *
     * @param aMetadata The range resource's metadata
     */
    public void setMetadata(final List<Metadata> aMetadata) {
        myMetadata = aMetadata;
    }

    /**
     * Gets the start canvas.
     *
     * @return The start canvas
     */
    public String getStartCanvas() {
        return myStartCanvas;
    }

    /**
     * Sets the start canvas.
     *
     * @param aStartCanvas A link from a sequence or range to a canvas that is contained within the sequence. On
     *        seeing this relationship, a client should advance to the specified canvas when beginning navigation
     *        through the sequence/range. This allows the client to begin with the first canvas that contains
     *        interesting content rather than requiring the user to skip past blank or empty canvases manually. A
     *        sequence or a range may have this relationship, and the target must be a canvas.
     */
    public void setStartCanvas(final String aStartCanvas) {
        myStartCanvas = aStartCanvas;
    }

    /**
     * Gets the range resource's thumbnail.
     *
     * @return The range resource's thumbnail
     */
    public String getThumbnail() {
        return myThumbnail;
    }

    /**
     * Sets the range resource's thumbnail.
     *
     * @param aThumbnail A thumbnail for the range resource
     */
    public void setThumbnail(final String aThumbnail) {
        myThumbnail = aThumbnail;
    }

    /**
     * Gets the range resource's viewing direction.
     *
     * @return The range resource's viewing direction
     */
    public String getViewingDirection() {
        return myViewingDirection;
    }

    /**
     * Sets the viewing direction of the range resource.
     *
     * @param aViewingDirection The direction that canvases of the resource should be presented when rendered for the
     *        user to navigate and/or read. A range or layer may have a viewing direction.
     */
    public void setViewingDirection(final String aViewingDirection) {
        myViewingDirection = aViewingDirection;
    }

    /**
     * Gets the range resource's viewing hint.
     *
     * @return The range resource's viewing hint
     */
    public String getViewingHint() {
        return myViewingHint;
    }

    /**
     * Sets the range resource's viewing hint.
     *
     * @param aViewingHint A viewing hint for the range resource
     */
    public void setViewingHint(final String aViewingHint) {
        myViewingHint = aViewingHint;
    }

}
