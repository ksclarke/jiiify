package info.freelibrary.jiiify.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

public class ImageUtilsTest {

    private final Logger LOGGER = LoggerFactory.getLogger(ImageUtilsTest.class);

    @Test
    public void testGetTilePaths() {

    }

    @Test
    public void testUseNativeLibs() {

    }

    @Test
    public void testUseNativeLibsBoolean() {

    }

    @Test
    public void testTransform() {

    }

    @Test
    public void testGetImageDimension() {

    }

    @Test
    public void testGetCenterFile() {

    }

    @Test
    public void testGetCenterDimension() {

    }

    @Test
    public void testRatioIntInt() {
        assertEquals(ImageUtils.ratio(329, 780), ImageUtils.ratio(1316, 3120));
        assertEquals(ImageUtils.ratio(292, 1024), ImageUtils.ratio(292, 1024));
    }

    @Test
    public void testRatioString() {
        final String size1 = "1316,3120";
        final String size2 = "292,1024";

        assertEquals(ImageUtils.ratio(329, 780), ImageUtils.ratio("1316,3120"));

        if (ImageUtils.ratio(329, 780).equals(ImageUtils.ratio(size1))) {
            LOGGER.debug(size1.substring(0, size1.indexOf(',') + 1));
        }

        if (ImageUtils.ratio(292, 1024).equals(ImageUtils.ratio(size2))) {
            LOGGER.debug(size2.substring(0, size2.indexOf(',') + 1));
        }
    }

}
