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

package info.freelibrary.jiiify.iiif.presentation.model;

import java.util.List;

import info.freelibrary.jiiify.iiif.presentation.model.other.Metadata;
import info.freelibrary.jiiify.iiif.presentation.model.other.ViewingDirection;

/**
 * Recommended URI Pattern: {scheme}://{host}/{prefix}/{identifier}/sequence/{name}
 * <ul>
 * <li>Each sequence must have at least one canvas and is likely to have more than one.</li>
 * <li>A manifest, sequence or canvas must not have a format.</li>
 * <li>A manifest or sequence must not have a height.</li>
 * <li>A manifest or sequence must not have a width.</li>
 * </ul>
 *
 * @author Ralf Eichinger
 */
@Deprecated
public class Sequence extends AbstractIiifResource {

    private List<Canvas> myCanvases;

    private String myDescription; // optional

    private String myLabel; // optional

    private List<Metadata> myMetadata; // optional

    private String myStartCanvas; // optional

    private String myThumbnail; // optional

    private String myViewingDirection; // optional

    private String myViewingHint; // optional

    /**
     * Creates a sequence.
     */
    public Sequence() {
        myType = "sc:Sequence";
    }

    /**
     * Creates a sequence with a label.
     *
     * @param aLabel A sequence may have a label, and if there are multiple sequences in a single manifest then they
     *        must have labels. The label should briefly convey the nature of sequence, such as “Current Page Order”.
     */
    public Sequence(final String aLabel) {
        this();

        assert aLabel != null;

        myLabel = aLabel;
    }

    /**
     * Gets a list of canvases.
     *
     * @return A list of canvases
     */
    public List<Canvas> getCanvases() {
        return myCanvases;
    }

    /**
     * Sets the sequence's canvases.
     *
     * @param aCanvases A list of canvases
     */
    public void setCanvases(final List<Canvas> aCanvases) {
        myCanvases = aCanvases;
    }

    /**
     * Gets the sequence's description.
     *
     * @return The sequence's description
     */
    public String getDescription() {
        return myDescription;
    }

    /**
     * Sets the sequence description.
     *
     * @param aDescription A sequence may have a description to further explain how it differs from other sequences.
     */
    public void setDescription(final String aDescription) {
        myDescription = aDescription;
    }

    /**
     * Sets the sequence ID.
     *
     * @param aID A sequence may have an id.
     */
    @Override
    public void setId(final String aID) {
        myID = aID;
    }

    /**
     * Gets the sequence label.
     *
     * @return The sequence label
     */
    public String getLabel() {
        return myLabel;
    }

    /**
     * Sets the sequence label.
     *
     * @param aLabel A label for the sequence
     */
    public void setLabel(final String aLabel) {
        myLabel = aLabel;
    }

    /**
     * Gets the sequence metadata.
     *
     * @return The sequence metadata
     */
    public List<Metadata> getMetadata() {
        return myMetadata;
    }

    /**
     * Sets the sequence metadata.
     *
     * @param aMetadataList A sequence may have metadata pairs associated with it to describe the difference between
     *        it and other sequences.
     */
    public void setMetadata(final List<Metadata> aMetadataList) {
        myMetadata = aMetadataList;
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
     * Gets the sequence thumbnail.
     *
     * @return The sequence thumbnail
     */
    public String getThumbnail() {
        return myThumbnail;
    }

    /**
     * Sets the sequence thumbnail.
     *
     * @param aThumbnail A sequence may have a thumbnail and should have a thumbnail if there are multiple sequences
     *        in a single manifest. Each of the thumbnails should be different.
     */
    public void setThumbnail(final String aThumbnail) {
        myThumbnail = aThumbnail;
    }

    /**
     * Gets the sequence's viewing direction.
     *
     * @return The sequence's viewing direction
     */
    public String getViewingDirection() {
        return myViewingDirection;
    }

    /**
     * Sets the sequence's viewing direction.
     *
     * @see ViewingDirection
     * @param aViewingDirection The direction that canvases of the resource should be presented when rendered for the
     *        user to navigate and/or read. A sequence may have a viewing direction, and it MAY be different to that
     *        of the manifest.
     */
    public void setViewingDirection(final String aViewingDirection) {
        myViewingDirection = aViewingDirection;
    }

    /**
     * Gets the sequence's viewing hint.
     *
     * @return The sequence's viewing hint
     */
    public String getViewingHint() {
        return myViewingHint;
    }

    /**
     * Sets the sequence's viewing hint.
     *
     * @param aViewingHint A manifest, sequence or range may have a viewing hint, with scope as per viewingDirection.
     */
    public void setViewingHint(final String aViewingHint) {
        myViewingHint = aViewingHint;
    }

}
