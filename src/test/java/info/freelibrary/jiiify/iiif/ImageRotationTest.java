
package info.freelibrary.jiiify.iiif;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests <code>ImageRotation</code>.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ImageRotationTest {

    /**
     * Test method for {@link info.freelibrary.jiiify.iiif.ImageRotation#ImageRotation(java.lang.String)}.
     */
    @Test
    public void testImageRotationBasic() throws InvalidRotationException {
        assertEquals(0.0f, new ImageRotation("0").getValue(), 0.0f);
    }

    /**
     * Test method for {@link info.freelibrary.jiiify.iiif.ImageRotation#ImageRotation(java.lang.String)}.
     */
    @Test(expected = InvalidRotationException.class)
    public void testImageRotationLessThanZero() throws InvalidRotationException {
        new ImageRotation("-10");
    }

    /**
     * Test method for {@link info.freelibrary.jiiify.iiif.ImageRotation#ImageRotation(java.lang.String)}.
     */
    @Test(expected = InvalidRotationException.class)
    public void testImageRotationGreaterThan360() throws InvalidRotationException {
        new ImageRotation("361");
    }

    /**
     * Test method for {@link info.freelibrary.jiiify.iiif.ImageRotation#ImageRotation(java.lang.String)}.
     */
    @Test
    public void testImageRotationFloat() throws InvalidRotationException {
        final ImageRotation rotation = new ImageRotation("10.1");

        assertEquals(10.1f, rotation.getValue(), 0.0f);
        assertEquals(false, rotation.isMirrored());
    }

    /**
     * Test method for {@link info.freelibrary.jiiify.iiif.ImageRotation#ImageRotation(java.lang.String)}.
     */
    @Test
    public void testImageRotationFloatMirrored() throws InvalidRotationException {
        final ImageRotation rotation = new ImageRotation("!10.1");

        assertEquals(10.1f, rotation.getValue(), 0.0f);
        assertEquals(true, rotation.isMirrored());
    }

    /**
     * Test method for {@link info.freelibrary.jiiify.iiif.ImageRotation#ImageRotation(java.lang.String)}.
     */
    @Test
    public void testImageRotationAsString() throws InvalidRotationException {
        assertEquals("360.0", new ImageRotation("!360").getValueAsString());
    }

    /**
     * Test method for {@link info.freelibrary.jiiify.iiif.ImageRotation#ImageRotation(java.lang.String)}.
     */
    @Test(expected = InvalidRotationException.class)
    public void testImageRotationBadValue() throws InvalidRotationException {
        new ImageRotation("oops");
    }

    @Test
    public void testToString() throws InvalidRotationException {
        assertEquals("90", new ImageRotation("90").toString());
        assertEquals("!90", new ImageRotation("!90").toString());
        assertEquals("50.25", new ImageRotation("50.25").toString());
        assertEquals("!50.25", new ImageRotation("!50.25").toString());
    }
}
