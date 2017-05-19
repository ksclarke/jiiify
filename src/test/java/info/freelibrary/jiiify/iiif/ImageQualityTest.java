
package info.freelibrary.jiiify.iiif;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * A test of {@link info.freelibrary.jiiify.iiif.ImageQuality}
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ImageQualityTest {

    /**
     * Tests <code>ImageQuality</code> constructor.
     *
     * @throws UnsupportedQualityException If an invalid value is passed into the constructor
     */
    @Test
    public void testImageQualityDefault() throws UnsupportedQualityException {
        assertEquals(ImageQuality.DEFAULT, new ImageQuality("default").getValue());
    }

    /**
     * Tests <code>ImageQuality</code> constructor.
     *
     * @throws UnsupportedQualityException If an invalid value is passed into the constructor
     */
    @Test
    public void testImageQualityColor() throws UnsupportedQualityException {
        assertEquals(ImageQuality.COLOR, new ImageQuality("color").getValue());
    }

    /**
     * Tests <code>ImageQuality</code> constructor.
     *
     * @throws UnsupportedQualityException If an invalid value is passed into the constructor
     */
    @Test
    public void testImageQualityGray() throws UnsupportedQualityException {
        assertEquals(ImageQuality.GRAY, new ImageQuality("gray").getValue());
    }

    /**
     * Tests <code>ImageQuality</code> constructor.
     *
     * @throws UnsupportedQualityException If an invalid value is passed into the constructor
     */
    @Test
    public void testImageQualityBitonal() throws UnsupportedQualityException {
        assertEquals(ImageQuality.BITONAL, new ImageQuality("bitonal").getValue());
    }

    @Test
    public void testToString() throws UnsupportedQualityException {
        assertEquals(ImageQuality.DEFAULT, new ImageQuality("default").toString());
    }
}
