
package info.freelibrary.jiiify.iiif;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests the <code>ImageFormat</code>.
 *
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class ImageFormatTest {

    /**
     * Tests the <code>ImageFormat</code> constructor with a valid value.
     *
     * @throws UnsupportedFormatException If the test has failed
     */
    @Test
    public void testConstructorWithValidFormat() throws UnsupportedFormatException {
        new ImageFormat(ImageFormat.JPG_EXT);
    }

    /**
     * Tests the <code>ImageFormat</code> constructor with an invalid value.
     *
     * @throws UnsupportedFormatException If the test has succeeded
     */
    @Test(expected = UnsupportedFormatException.class)
    public void testConstructorWithInvalidFormat() throws UnsupportedFormatException {
        new ImageFormat("doc");
    }

    /**
     * Tests the <code>ImageFormat</code>'s <code>getExtension()</code>.
     *
     * @throws UnsupportedFormatException If the test has failed
     */
    @Test
    public void testGetExtension() throws UnsupportedFormatException {
        assertEquals(ImageFormat.JP2_EXT, new ImageFormat(ImageFormat.JP2_EXT).getExtension());
    }

    /**
     * Tests the <code>ImageFormat</code>'s <code>getMIMEType()</code>.
     *
     * @throws UnsupportedFormatException If the test has failed
     */
    @Test
    public void testGetMIMEType() throws UnsupportedFormatException {
        assertEquals(ImageFormat.JP2_MIME_TYPE, new ImageFormat(ImageFormat.JP2_EXT).getMIMEType());
    }

    /**
     * Tests the <code>ImageFormat</code>'s <code>getExtension(String)</code>.
     */
    @Test
    public void testGetExtensionString() {
        assertEquals(ImageFormat.JP2_EXT, ImageFormat.getExtension(ImageFormat.JP2_MIME_TYPE));
    }

    /**
     * Tests the <code>ImageFormat</code>'s <code>getMIMEType(String)</code>.
     */
    @Test
    public void testGetMIMETypeString() {
        assertEquals(ImageFormat.JP2_MIME_TYPE, ImageFormat.getMIMEType(ImageFormat.JP2_EXT));
    }

    @Test
    public void testToString() throws UnsupportedFormatException {
        assertEquals(ImageFormat.JP2_EXT, new ImageFormat("jp2").toString());
    }
}
