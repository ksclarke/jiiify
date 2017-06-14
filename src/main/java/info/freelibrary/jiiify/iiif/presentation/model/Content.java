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

/**
 * Content resources such as images or texts that are associated with a canvas.
 * <ul>
 * <li>A canvas or content resource must not have a viewing direction.</li>
 * </ul>
 *
 * @author Ralf Eichinger
 */
@Deprecated
public class Content extends AbstractIiifResource {

    protected List<Metadata> myMetadata; // optional

    protected String myDescription; // optional

    protected String myThumbnail; // optional

    protected String myFormat; // optional

    protected int myHeight; // optional

    protected int myWidth; // optional

    protected String myViewingHint; // optional

    /**
     * Creates IIIF content resource.
     *
     * @param aID A content resource must have an ID unless it is embedded in the response, and it must be the HTTP(S)
     *        URI at which the resource is published.
     */
    public Content(final String aID) {
        assert aID != null;
        myID = aID;
    }

    /**
     * Gets content resource's metadata.
     *
     * @return The content's metadata
     */
    public List<Metadata> getMetadata() {
        return myMetadata;
    }

    /**
     * Sets the content resource metadata.
     *
     * @param aMetadata A content resource may have metadata pairs associated with it.
     */
    public void setMetadata(final List<Metadata> aMetadata) {
        myMetadata = aMetadata;
    }

    /**
     * Gets content description.
     *
     * @return The content description
     */
    public String getDescription() {
        return myDescription;
    }

    /**
     * Sets content description.
     *
     * @param aDescription A content resource may have a description.
     */
    public void setDescription(final String aDescription) {
        myDescription = aDescription;
    }

    /**
     * Gets the content thumbnail.
     *
     * @return The content thumbnail
     */
    public String getThumbnail() {
        return myThumbnail;
    }

    /**
     * Sets the content format.
     *
     * @param aThumbnail A content resource may have a thumbnail and should have a thumbnail if it is an option in a
     *        choice of resources.
     */
    public void setThumbnail(final String aThumbnail) {
        myThumbnail = aThumbnail;
    }

    /**
     * Gets the content format.
     *
     * @return The content's format
     */
    public String getFormat() {
        return myFormat;
    }

    /**
     * Sets the content format.
     *
     * @param aFormat The specific media type (often called a MIME type) of a content resource, for example
     *        “image/jpeg”. This is important for distinguishing text in XML from plain text, for example. A content
     *        resource may have a format, and if so, it must be the value of the Content-Type header returned when the
     *        resource is dereferenced. N.B. This is different to the formats property in the Image API, which gives
     *        the extension to use within that API. It would be inappropriate to use in this case, as format can be
     *        used with any content resource, not just images.
     */
    public void setFormat(final String aFormat) {
        myFormat = aFormat;
    }

    /**
     * Gets the content height.
     *
     * @return The content's height
     */
    public int getHeight() {
        return myHeight;
    }

    /**
     * Sets the content height.
     *
     * @param aHeight The height of a canvas or image resource. For images, this is in pixels. No particular units are
     *        required for canvases, as the dimensions provide an aspect ratio for the resources to be located within
     *        rather than measuring any physical property of the object. Content resources may have a height, given in
     *        pixels, if appropriate.
     */
    public void setHeight(final int aHeight) {
        myHeight = aHeight;
    }

    /**
     * Gets the content width.
     *
     * @return The content width
     */
    public int getWidth() {
        return myWidth;
    }

    /**
     * Sets the content width.
     *
     * @param aWidth The width of a canvas or image resource. For images, this is in pixels. No particular units are
     *        required for canvases. Content resources may have a height, given in pixels, if appropriate.
     */
    public void setWidth(final int aWidth) {
        myWidth = aWidth;
    }

    /**
     * Gets the content viewing hint.
     *
     * @return The content viewing hint
     */
    public String getViewingHint() {
        return myViewingHint;
    }

    /**
     * Sets the content viewing hint.
     *
     * @param aViewingHint A content resource may have a viewing hint but there are no defined values in this
     *        specification.
     */
    public void setViewingHint(final String aViewingHint) {
        myViewingHint = aViewingHint;
    }

}