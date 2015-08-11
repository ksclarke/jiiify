package info.freelibrary.jiiify.iiif;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.qos.logback.classic.Level;

import info.freelibrary.jiiify.util.LoggingUtils;

public class ImageRequestTest {

    @Test
    public void testImageRequest() throws IIIFException {
        new ImageRequest("/iiif/asdf/full/full/0/default.jpg");
    }

    @Test
    public void testImageRequestWithoutLogging() throws IIIFException {
        final String logLevel = LoggingUtils.getLogLevel(ImageRequest.class);
        LoggingUtils.setLogLevel(ImageRequest.class, Level.OFF.levelStr);
        new ImageRequest("/iiif/asdf/full/full/0/default.jpg");
        LoggingUtils.setLogLevel(ImageRequest.class, logLevel);
    }

    @Test(expected = IIIFException.class)
    public void testImageRequestMissingDot() throws IIIFException {
        new ImageRequest("/iiif/asdf/full/full/0/default-jpg");
    }

    @Test
    public void testToString() throws IIIFException {
        final ImageRequest request = new ImageRequest("/iiif/asdf/full/full/0/default.jpg");
        assertEquals("/iiif/asdf/full/full/0/default.jpg", request.toString());
    }

}
