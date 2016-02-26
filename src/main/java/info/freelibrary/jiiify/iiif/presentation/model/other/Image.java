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

/**
 *
 * @author Ralf Eichinger
 */
public class Image {

    private String myID;

    private static final String MOTIVATION = "sc:painting";

    private static final String TYPE = "oa:Annotation";

    private String myOn;

    private Resource myResource;

    /**
     * Creates image.
     */
    public Image() {
    }

    /**
     * Creates images with ID.
     *
     * @param aID An image ID
     */
    public Image(final String aID) {
        myID = aID;
    }

    /**
     * Gets the image ID.
     *
     * @return The image ID
     */
    public String getId() {
        return myID;
    }

    /**
     * Gets image's resource.
     *
     * @return The image's resource
     */
    public Resource getResource() {
        return myResource;
    }

    /**
     * Sets the image's resource.
     *
     * @param aResource An image resource
     */
    public void setResource(final Resource aResource) {
        myResource = aResource;
    }

    /**
     * Gets image motivation.
     *
     * @return The image motivation
     */
    public String getMotivation() {
        return MOTIVATION;
    }

    /**
     * Gets image type.
     *
     * @return The image type
     */
    public String getType() {
        return TYPE;
    }

    /**
     * Gets the image's "on".
     *
     * @return The image's "on"
     */
    public String getOn() {
        return myOn;
    }

    /**
     * Sets the image's "on".
     *
     * @param aOn An "on" for the image
     */
    public void setOn(final String aOn) {
        myOn = aOn;
    }

}
