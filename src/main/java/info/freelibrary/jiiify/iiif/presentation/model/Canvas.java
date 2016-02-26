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

import info.freelibrary.jiiify.iiif.presentation.model.other.Image;
import info.freelibrary.jiiify.iiif.presentation.model.other.Metadata;

/**
 * <p>
 * The canvas represents an individual page or view and acts as a central point for laying out the different content
 * resources that make up the display. As with sequences, the name should not begin with a number. Suggested patterns
 * are “f1r” or “p1”.
 * </p>
 * <p>
 * Recommended URI Pattern: {scheme}://{host}/{prefix}/{identifier}/canvas/{name}
 * </p>
 * <ul>
 * <li>Each canvas should have one or more content resources associated with it. Zero is possible but unlikely; it
 * represents the case where the page exists (or existed) but has not been digitized.</li>
 * <li>A manifest, sequence or canvas must not have a format.</li>
 * <li>A canvas or content resource must not have a viewing direction.</li>
 * </ul>
 *
 * @author Ralf Eichinger
 */
public class Canvas extends AbstractIiifResource {

    private String myDescription; // optional

    private final int myHeight; // required

    private List<Image> myImages;

    private final String myLabel; // required

    private List<Metadata> myMetadata; // optional

    private String myThumbnail; // recommended

    private String myViewingHint; // optional

    private int myWidth; // required

    /**
     * Creates a IIIF canvas with ID, label, height, and width.
     *
     * @param aID A canvas ID
     * @param aLabel A canvas label
     * @param aHeight A canvas height
     * @param aWidth A canvas width
     */
    public Canvas(final String aID, final String aLabel, final int aHeight, final int aWidth) {
        assert aID != null;
        assert aLabel != null;
        assert aHeight > -1;
        assert aWidth > -1;

        myID = aID;
        myHeight = aHeight;
        myLabel = aLabel;
        myWidth = aWidth;

        myType = "sc:Canvas";
    }

    /**
     * Creates a IIIF canvas with ID, label, height, width, and thumbnail.
     *
     * @param aID A canvas must have an id, and it must be an HTTP(S) URI. The canvas’s JSON representation should be
     *        published at that URI.
     * @param aLabel A canvas must have a label, and it should be the page or view label such as “p. 1”, “front”, or
     *        “north view”.
     * @param aHeight The height of a canvas or image resource. For images, this is in pixels. No particular units are
     *        required for canvases, as the dimensions provide an aspect ratio for the resources to be located within
     *        rather than measuring any physical property of the object. A canvas must have a height, which does not
     *        have a unit type. It merely conveys, along with width, an aspect ratio.
     * @param aWidth The width of a canvas or image resource. For images, this is in pixels. No particular units are
     *        required for canvases. A canvas must have a height, which does not have a unit type. It merely conveys,
     *        along with width, an aspect ratio.
     * @param aThumbnail A canvas may have a thumbnail and should have a thumbnail if there are multiple images or
     *        resources that make up the representation.
     */
    public Canvas(final String aID, final String aLabel, final int aHeight, final int aWidth,
            final String aThumbnail) {
        this(aID, aLabel, aHeight, aWidth);
        myThumbnail = aThumbnail;
    }

    /**
     * Gets the canvas description.
     *
     * @return The canvas description
     */
    public String getDescription() {
        return myDescription;
    }

    /**
     * @param aDescription A canvas may have a description to describe particular features of the view.
     */
    public void setDescription(final String aDescription) {
        myDescription = aDescription;
    }

    /**
     * Gets the canvas height.
     *
     * @return The canvas height
     */
    public int getHeight() {
        return myHeight;
    }

    /**
     * Sets the canvas height.
     *
     * @param aHeight The canvas height
     */
    public void setHeight(final int aHeight) {
    }

    /**
     * Gets the canvas images.
     *
     * @return The canvas images
     */
    public List<Image> getImages() {
        return myImages;
    }

    /**
     * Sets the canvas images.
     *
     * @param aImages The canvas images
     */
    public void setImages(final List<Image> aImages) {
        myImages = aImages;
    }

    /**
     * Gets the canvas label.
     *
     * @return The canvas label
     */
    public String getLabel() {
        return myLabel;
    }

    /**
     * Gets the canvas metadata.
     *
     * @return The canvas metadata
     */
    public List<Metadata> getMetadata() {
        return myMetadata;
    }

    /**
     * Sets the canvas metadata.
     *
     * @param aMetadata A canvas may have metadata pairs associated with it to describe its particular features.
     */
    public void setMetadata(final List<Metadata> aMetadata) {
        myMetadata = aMetadata;
    }

    /**
     * Gets the canvas thumbnail.
     *
     * @return The canvas thumbnail
     */
    public String getThumbnail() {
        return myThumbnail;
    }

    /**
     * @param thumbnail A canvas may have a thumbnail and should have a thumbnail if there are multiple images or
     *        resources that make up the representation.
     */
    public void setThumbnail(final String thumbnail) {
        myThumbnail = thumbnail;
    }

    /**
     * Gets the viewing hint.
     *
     * @return The viewing hint
     */
    public String getViewingHint() {
        return myViewingHint;
    }

    /**
     * Sets the canvas' viewing hint.
     *
     * @param viewingHint A manifest, sequence or range may have a viewing hint, with scope as per viewingDirection. A
     *        canvas may have a viewing hint, and the only hint defined by this specification for canvases is
     *        “non-paged”. “non-paged” is only valid if the canvas is within a manifest, sequence or range that is
     *        “paged”, and the particular canvas must not be displayed in a page-turning viewer.
     */
    public void setViewingHint(final String viewingHint) {
    }

    /**
     * Gets the canvas width.
     *
     * @return The canvas width
     */
    public int getWidth() {
        return myWidth;
    }

    /**
     * Sets the canvas width
     *
     * @param aWidth The canvas width
     */
    public void setWidth(final int aWidth) {
        myWidth = aWidth;
    }

}
