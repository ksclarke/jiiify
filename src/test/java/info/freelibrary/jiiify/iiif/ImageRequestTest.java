
package info.freelibrary.jiiify.iiif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import info.freelibrary.jiiify.util.LoggingUtils;

import ch.qos.logback.classic.Level;

public class ImageRequestTest {

    private static final String IMAGE_REQUEST = "/iiif/asdf/full/full/0/default.jpg";

    @Test
    public void testImageRequest() throws IIIFException {
        new ImageRequest(IMAGE_REQUEST);
    }

    @Test
    public void testImageRequestWithoutLogging() throws IIIFException {
        final String logLevel = LoggingUtils.getLogLevel(ImageRequest.class);
        LoggingUtils.setLogLevel(ImageRequest.class, Level.OFF.levelStr);
        new ImageRequest(IMAGE_REQUEST);
        LoggingUtils.setLogLevel(ImageRequest.class, logLevel);
    }

    @Test(expected = IIIFException.class)
    public void testImageRequestMissingDot() throws IIIFException {
        new ImageRequest("/iiif/asdf/full/full/0/default-jpg");
    }

    @Test
    public void testToString() throws IIIFException {
        final ImageRequest request = new ImageRequest(IMAGE_REQUEST);
        assertEquals(IMAGE_REQUEST, request.toString());
    }

    @Test
    public void testClone() {
        try {
            final ImageRequest request = new ImageRequest(IMAGE_REQUEST);
            final ImageRequest clone = request.clone();

            assertTrue(request != clone);
            assertTrue(request.getClass() == clone.getClass());
            assertEquals(request, clone);
        } catch (final IIIFException details) {
            fail(details.getMessage());
        }
    }

    @Test
    public void testSetSize() {
        try {
            final ImageRequest request = new ImageRequest(IMAGE_REQUEST);
            final ImageSize size = new ImageSize("100,");

            assertNotEquals(request.getSize(), size);
            request.setSize(size);
            assertEquals(request.getSize(), size);
        } catch (final IIIFException details) {
            fail(details.getMessage());
        }
    }

    @Test
    public void testSetFormat() {
        try {
            final ImageRequest request = new ImageRequest(IMAGE_REQUEST);
            final ImageFormat format = new ImageFormat("png");

            assertNotEquals(request.getFormat(), format);
            request.setFormat(format);
            assertEquals(request.getFormat(), format);
        } catch (final IIIFException details) {
            fail(details.getMessage());
        }
    }

    @Test
    public void testSetQuality() {
        try {
            final ImageRequest request = new ImageRequest(IMAGE_REQUEST);
            final ImageQuality quality = new ImageQuality("gray");

            assertNotEquals(request.getQuality(), quality);
            request.setQuality(quality);
            assertEquals(request.getQuality(), quality);
        } catch (final IIIFException details) {
            fail(details.getMessage());
        }
    }

    @Test
    public void testSetRotation() {
        try {
            final ImageRequest request = new ImageRequest(IMAGE_REQUEST);
            final ImageRotation rotation = new ImageRotation("90");

            assertNotEquals(request.getRotation(), rotation);
            request.setRotation(rotation);
            assertEquals(request.getRotation(), rotation);
        } catch (final IIIFException details) {
            fail(details.getMessage());
        }
    }

    @Test
    public void testSetRegion() {
        try {
            final ImageRequest request = new ImageRequest(IMAGE_REQUEST);
            final ImageRegion region = new ImageRegion("100,100,100,100");

            assertNotEquals(request.getRegion(), region);
            request.setRegion(region);
            assertEquals(request.getRegion(), region);
        } catch (final IIIFException details) {
            fail(details.getMessage());
        }
    }

}
