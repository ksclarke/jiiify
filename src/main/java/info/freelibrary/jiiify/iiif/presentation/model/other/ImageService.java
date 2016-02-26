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

import java.util.List;

/**
 * @author Ralf Eichinger
 */
public class ImageService extends Service {

    private int myHeight;

    private int myWidth;

    private List<Tile> myTiles;

    /**
     * Gets the image service height.
     *
     * @return The image service height
     */
    public int getHeight() {
        return myHeight;
    }

    /**
     * Sets the image service height.
     *
     * @param aHeight An image service height
     */
    public void setHeight(final int aHeight) {
        myHeight = aHeight;
    }

    /**
     * Gets the image service width.
     *
     * @return The image service width
     */
    public int getWidth() {
        return myWidth;
    }

    /**
     * Sets the image service width.
     *
     * @param aWidth An image service width
     */
    public void setWidth(final int aWidth) {
        myWidth = aWidth;
    }

    /**
     * Gets the image service tiles.
     *
     * @return The image service tiles
     */
    public List<Tile> getTiles() {
        return myTiles;
    }

    /**
     * Sets the image service tiles.
     *
     * @param aTilesList A list of tiles
     */
    public void setTiles(final List<Tile> aTilesList) {
        myTiles = aTilesList;
    }

}
