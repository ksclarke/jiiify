
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
 * @author Kevin S. Clarke (<href="mailto:ksclarke@ksclarke.io">ksclarke@ksclarke.io</a>)
 */
public interface ImageObject {

    /**
     * Changes the image object by extracting a region from the whole.
     *
     * @param aRegion A IIIF image region
     */
    public void extractRegion(final ImageRegion aRegion) throws IOException;

    /**
     * Changes the image object by resizing it.
     *
     * @param aSize A IIIF image size
     */
    public void resize(final ImageSize aSize) throws IOException;

    /**
     * Changes the image object by rotating it.
     *
     * @param aRotation A IIIF image rotation
     */
    public void rotate(final ImageRotation aRotation) throws IOException;

    /**
     * Changes the image object by adjusting its quality.
     *
     * @param aQuality A IIIF image quality
     */
    public void adjustQuality(final ImageQuality aQuality) throws IOException;

    /**
     * Writes the image object to a Vertx {@link io.vertx.core.buffer.Buffer}.
     *
     * @param aFileExt A file extension to indicate desired image output format
     */
    public Buffer toBuffer(final String aFileExt) throws IOException;

}
