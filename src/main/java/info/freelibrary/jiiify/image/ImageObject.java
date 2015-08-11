
package info.freelibrary.jiiify.image;

import java.io.File;
import java.io.IOException;

import info.freelibrary.jiiify.iiif.ImageFormat;
import info.freelibrary.jiiify.iiif.ImageQuality;
import info.freelibrary.jiiify.iiif.ImageRegion;
import info.freelibrary.jiiify.iiif.ImageRotation;
import info.freelibrary.jiiify.iiif.ImageSize;

/**
 * Jiiify's internal representation of an image.
 *
 * @author Kevin S. Clarke (<href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>)
 */
public interface ImageObject {

    /**
     * Changes the image object by extracting a region from the whole.
     */
    public void extractRegion(ImageRegion aRegion) throws IOException;

    /**
     * Changes the image object by resizing it.
     */
    public void resize(ImageSize aSize) throws IOException;

    /**
     * Changes the image object by rotating it.
     */
    public void rotate(ImageRotation aRotation) throws IOException;

    /**
     * Changes the image object by adjusting its quality.
     */
    public void adjustQuality(ImageQuality aQuality) throws IOException;

    /**
     * Changes the image object by changing its format.
     */
    public void changeFormat(ImageFormat aFormat) throws IOException;

    /**
     * Writes the image object to the supplied file.
     */
    public void write(File aOutputFile) throws IOException;

}
