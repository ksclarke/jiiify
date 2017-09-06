
package info.freelibrary.jiiify.image;

import java.io.IOException;

import info.freelibrary.jiiify.iiif.ImageQuality;
import info.freelibrary.jiiify.iiif.ImageRegion;
import info.freelibrary.jiiify.iiif.ImageRotation;
import info.freelibrary.jiiify.iiif.ImageSize;

import io.vertx.core.buffer.Buffer;

/**
 * Jiiify's internal representation of an image.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public interface ImageObject {

    /**
     * Returns the width of the image.
     *
     * @return The width of the image.
     */
    int getWidth();

    /**
     * Returns the height of the image.
     *
     * @return The height of the image.
     */
    int getHeight();

    /**
     * Frees resources associated with the image.
     */
    void free();

    /**
     * Changes the image object by extracting a region from the whole.
     *
     * @param aRegion A IIIF image region
     * @throws IOException If there is trouble reading the image region
     */
    void extractRegion(ImageRegion aRegion) throws IOException;

    /**
     * Changes the image object by resizing it.
     *
     * @param aSize A IIIF image size
     * @throws IOException If there is trouble reading the image size
     */
    void resize(ImageSize aSize) throws IOException;

    /**
     * Changes the image object by rotating it.
     *
     * @param aRotation A IIIF image rotation
     * @throws IOException If there is trouble reading the image rotation
     */
    void rotate(ImageRotation aRotation) throws IOException;

    /**
     * Changes the image object by adjusting its quality.
     *
     * @param aQuality A IIIF image quality
     * @throws IOException If there is trouble reading the image quality
     */
    void adjustQuality(ImageQuality aQuality) throws IOException;

    /**
     * Writes the image object to a Vertx {@link io.vertx.core.buffer.Buffer}.
     *
     * @param aFileExt A file extension to indicate desired image output format
     * @throws IOException If there is trouble writing the image to the Vert.x buffer
     * @return A Vert.x {@link io.vertx.core.buffer.Buffer}
     */
    Buffer toBuffer(String aFileExt) throws IOException;

}
