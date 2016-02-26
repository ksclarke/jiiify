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
 * @author Ralf Eichinger
 */
public class Tile {

    private int myWidth;

    private int[] myScaleFactors;

    /**
     * Gets the tile width.
     *
     * @return The width of the tile
     */
    public int getWidth() {
        return myWidth;
    }

    /**
     * Sets the tile width.
     *
     * @param aWidth A tile width
     */
    public void setWidth(final int aWidth) {
        myWidth = aWidth;
    }

    /**
     * Gets the tile's scale factors.
     *
     * @return The tile's scale factors
     */
    public int[] getScaleFactors() {
        return myScaleFactors;
    }

    /**
     * Sets the tile's scale factors
     *
     * @param aScaleFactors
     */
    public void setScaleFactors(final int[] aScaleFactors) {
        myScaleFactors = aScaleFactors;
    }

}
