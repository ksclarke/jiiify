
package info.freelibrary.jiiify.iiif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * A test of {@link info.freelibrary.jiiify.iiif.ImageSize}
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ImageSizeTest {

    @Test
    public void testImageSizeFull() {
        ImageSize size;

        try {
            size = new ImageSize(ImageSize.FULL);
            assertTrue(size.isFullSize());
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }

        try {
            size = new ImageSize("pct:100");
            assertTrue(size.isFullSize());
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }

        try {
            size = new ImageSize("pct:50");
            assertFalse(size.isFullSize());
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }

        try {
            size = new ImageSize("50,");
            assertFalse(size.isFullSize());
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }
    }

    @Test
    public void testImageSizePercent() {
        try {
            new ImageSize("pct:50");
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }

        try {
            new ImageSize("pct:0");
            fail("Failed to throw an exception for a 0 value");
        } catch (final InvalidSizeException details) {
            // Expected exception
        }

        try {
            new ImageSize("pct:101");
            fail("Failed to throw an exception for a 101 value");
        } catch (final InvalidSizeException details) {
            // Expected exception
        }

        try {
            new ImageSize("pct:bad_percent");
            fail("Failed to throw an exception for a non-int percent");
        } catch (final InvalidSizeException details) {
            // Expected exception
        }
    }

    @Test
    public void testImageSizeHeight() {
        try {
            new ImageSize(",200");
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }

        try {
            new ImageSize(",bad_size");
            fail("Failed to throw exception on non-integer height value");
        } catch (final InvalidSizeException details) {
            // Expected exception
        }
    }

    @Test
    public void testHasWidth() throws InvalidSizeException {
        assertTrue(new ImageSize("10,10").hasWidth());
    }

    @Test
    public void testHasHeight() throws InvalidSizeException {
        assertTrue(new ImageSize("10,10").hasHeight());
    }

    @Test
    public void testToString() throws InvalidSizeException {
        assertEquals("full", new ImageSize("full").toString());
        assertEquals("pct:50", new ImageSize("pct:50").toString());
        assertEquals("100,", new ImageSize("100,").toString());
        assertEquals(",100", new ImageSize(",100").toString());
        assertEquals("60,60", new ImageSize("60,60").toString());
        assertEquals("!60,60", new ImageSize("!60,60").toString());
    }

    @Test
    public void testImageSizeWidth() {
        try {
            new ImageSize("200,");
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }

        try {
            new ImageSize("bad_size,");
            fail("Failed to throw exception on non-integer width value");
        } catch (final InvalidSizeException details) {
            // Expected exception
        }
    }

    @Test
    public void testImageSizeWidthHeight() {
        try {
            new ImageSize("200,200");
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }

        try {
            new ImageSize("bad_size,200");
            fail("Failed to catch bad width");
        } catch (final InvalidSizeException details) {
            // Expected exception
        }

        try {
            new ImageSize("200,bad_size");
            fail("Failed to catch bad height");
        } catch (final InvalidSizeException details) {
            // Expected exception
        }
    }

    @Test
    public void canBeScaled() {
        try {
            assertTrue(new ImageSize("!200,200").isScalable());
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }

        try {
            assertFalse(new ImageSize("200,200").isScalable());
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }
    }

    @Test
    public void testScalableRequest() {
        try {
            new ImageSize("!200,200");
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }

        try {
            new ImageSize("!bad_size,200");
            fail("Failed to catch bad width");
        } catch (final InvalidSizeException details) {
            // Expected exception
        }

        try {
            new ImageSize("!200,bad_size");
            fail("Failed to catch bad height");
        } catch (final InvalidSizeException details) {
            // Expected exception
        }
    }

    @Test
    public void testGetPercentage() {
        try {
            assertEquals(50, new ImageSize("pct:50").getPercentage());
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }
    }

    @Test
    public void testIsPercentage() {
        try {
            assertTrue(new ImageSize("pct:100").isPercentage());
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }

        try {
            assertFalse(new ImageSize("100,").isPercentage());
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }
    }

    @Test
    public void testImageSizeCommas() {
        try {
            new ImageSize("200,200,200");
            fail("Failed to catch extra comma");
        } catch (final InvalidSizeException details) {
            // Expected exception
        }

        try {
            new ImageSize(",");
            fail("Failed to catch missing width and height");
        } catch (final InvalidSizeException details) {
            // Expected exception
        }

        try {
            new ImageSize("200,200,");
            fail("Failed to catch extra comma");
        } catch (final InvalidSizeException details) {
            // Expected exception
        }
    }

    @Test
    public void testEmptyImageSize() {
        try {
            new ImageSize("");
            fail("Failed to catch empty ImageSize string");
        } catch (final InvalidSizeException details) {
            // Expected exception
        }

        try {
            new ImageSize(null);
            fail("Failed to catch null ImageSize string");
        } catch (final InvalidSizeException details) {
            // Expected exception
        }
    }

    @Test(expected = InvalidSizeException.class)
    public void testNonIntegerSize() throws InvalidSizeException {
        new ImageSize("invalid size");
    }

    @Test
    public void testGetHeight() {
        try {
            assertEquals(200, new ImageSize("100,200").getHeight());
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }

        try {
            assertEquals(0, new ImageSize("pct:100").getHeight());
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }
    }

    @Test
    public void testGetHeightMinimum() {
        try {
            assertEquals(50, new ImageSize("100,200").getHeight(50, 50));
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }

        try {
            assertEquals(200, new ImageSize("100,200").getHeight(300, 50));
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }

        try {
            assertEquals(50, new ImageSize("pct:100").getHeight(50, 100));
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }
    }

    @Test
    public void testGetWidth() {
        try {
            assertEquals(100, new ImageSize("100,200").getWidth());
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }

        try {
            assertEquals(0, new ImageSize("pct:100").getWidth());
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }
    }

    @Test
    public void testGetWidthMinimum() {
        try {
            assertEquals(50, new ImageSize("100,200").getWidth(50, 50));
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }

        try {
            assertEquals(100, new ImageSize("100,200").getWidth(300, 50));
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }

        try {
            assertEquals(50, new ImageSize("pct:100").getWidth(50, 100));
        } catch (final InvalidSizeException details) {
            fail(details.getMessage());
        }
    }

}
