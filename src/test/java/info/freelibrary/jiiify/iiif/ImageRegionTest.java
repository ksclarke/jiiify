
package info.freelibrary.jiiify.iiif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import info.freelibrary.jiiify.iiif.ImageRegion.Region;

/**
 * A test of {@link info.freelibrary.jiiify.iiif.ImageRegion}
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ImageRegionTest {

    @Test
    public void testImageRegion() {
        try {
            new ImageRegion("0,0,100,100");
        } catch (final InvalidRegionException details) {
            fail(details.getMessage());
        }

        // Check value of the static final FULL
        assertEquals("full", ImageRegion.FULL);

        // Check our Region enumeration values
        assertEquals(4, Region.values().length);
        assertEquals(Region.X, Region.valueOf("X"));
        assertEquals(Region.Y, Region.valueOf("Y"));
        assertEquals(Region.WIDTH, Region.valueOf("WIDTH"));
        assertEquals(Region.HEIGHT, Region.valueOf("HEIGHT"));
    }

    @Test
    public void testImageRegionDimensions() {
        try {
            new ImageRegion("0,0,100,bad");
        } catch (final InvalidRegionException details) {
            // Expected exception
        }

        try {
            new ImageRegion("0,0,100,100,0");
        } catch (final InvalidRegionException details) {
            // Expected exception
        }
    }

    @Test
    public void testImageRegionEmptyDimensions() {
        try {
            new ImageRegion(null);
        } catch (final InvalidRegionException details) {
            // Expected exception
        }

        try {
            new ImageRegion("");
        } catch (final InvalidRegionException details) {
            // Expected exception
        }
    }

    @Test
    public void testGetInt() {
        try {
            final ImageRegion region = new ImageRegion("10,20,60,80");

            assertEquals(10, region.getInt(Region.X));
            assertEquals(20, region.getInt(Region.Y));
            assertEquals(60, region.getInt(Region.WIDTH));
            assertEquals(80, region.getInt(Region.HEIGHT));
        } catch (final InvalidRegionException details) {
            fail(details.getMessage());
        }
    }

    @Test
    public void testGetFloat() {
        try {
            final ImageRegion region = new ImageRegion("10,20,60,80");

            assertEquals(10f, region.getFloat(Region.X), 0.0000);
            assertEquals(20f, region.getFloat(Region.Y), 0.0000);
            assertEquals(60f, region.getFloat(Region.WIDTH), 0.0000);
            assertEquals(80f, region.getFloat(Region.HEIGHT), 0.0000);
        } catch (final InvalidRegionException details) {
            fail(details.getMessage());
        }
    }

    @Test
    public void testUsesPercentages() {
        try {
            assertTrue(new ImageRegion("pct:100,100,100,100").usesPercentages());
        } catch (final InvalidRegionException details) {
            fail(details.getMessage());
        }

        try {
            assertFalse(new ImageRegion("100,100,100,100").usesPercentages());
        } catch (final InvalidRegionException details) {
            fail(details.getMessage());
        }
    }

    @Test
    public void testToString() throws InvalidRegionException {
        assertEquals("100,100,100,100", new ImageRegion("100,100,100,100").toString());
        assertEquals("10.5,10.5,10.5,10.5", new ImageRegion("10.5,10.5,10.5,10.5").toString());
        assertEquals("full", new ImageRegion("pct:100,100,100,100").toString());
        assertEquals("pct:50,50,50,50", new ImageRegion("pct:50,50,50,50").toString());
    }

    @Test
    public void testIsFullImage() {
        try {
            assertTrue(new ImageRegion("pct:100,100,100,100").isFullImage());
        } catch (final InvalidRegionException details) {
            fail(details.getMessage());
        }

        try {
            assertFalse(new ImageRegion("pct:100,99,100,100").isFullImage());
        } catch (final InvalidRegionException details) {
            fail(details.getMessage());
        }

        try {
            assertTrue(new ImageRegion(ImageRegion.FULL).isFullImage());
        } catch (final InvalidRegionException details) {
            fail(details.getMessage());
        }
    }

}
