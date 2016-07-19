
package info.freelibrary.jiiify.iiif;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.TestConstants;

/**
 * An exception thrown when the supplied format is not a valid IIIF format.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class UnsupportedFormatExceptionTest {

    private Locale myDefaultLocale;

    /**
     * Sets up the tests of <code>UnsupportedFormatException</code>.
     *
     * @throws Exception If the test set up fails
     */
    @Before
    public void setUp() throws Exception {
        myDefaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    /**
     * Tears down the tests of <code>UnsupportedFormatException</code>.
     *
     * @throws Exception If the test tear down fails
     */
    @After
    public void tearDown() throws Exception {
        Locale.setDefault(myDefaultLocale);
    }

    @Test(expected = UnsupportedFormatException.class)
    public void testEmptyConstructor() throws UnsupportedFormatException {
        throw new UnsupportedFormatException();
    }

    @Test
    public void testString() {
        try {
            throw new UnsupportedFormatException(MessageCodes.TEST_001);
        } catch (final UnsupportedFormatException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testLocaleString() {
        try {
            throw new UnsupportedFormatException(Locale.US, MessageCodes.TEST_001);
        } catch (final UnsupportedFormatException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testLocaleStringVarargs() {
        try {
            throw new UnsupportedFormatException(Locale.US, MessageCodes.TEST_002, TestConstants.DETAIL_ONE,
                    TestConstants.DETAIL_TWO);
        } catch (final UnsupportedFormatException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

    @Test(expected = UnsupportedFormatException.class)
    public void testThrowable() throws UnsupportedFormatException {
        throw new UnsupportedFormatException(new Exception());
    }

    @Test
    public void testThrowableString() {
        try {
            throw new UnsupportedFormatException(new Exception(), MessageCodes.TEST_001);
        } catch (final UnsupportedFormatException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testThrowableStringVarargs() {
        try {
            throw new UnsupportedFormatException(new Exception(), MessageCodes.TEST_002, TestConstants.DETAIL_ONE,
                    TestConstants.DETAIL_TWO);
        } catch (final UnsupportedFormatException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

    @Test
    public void testThrowableLocaleString() {
        try {
            throw new UnsupportedFormatException(new Exception(), Locale.US, MessageCodes.TEST_001);
        } catch (final UnsupportedFormatException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testThrowableLocaleStringVarargs() {
        try {
            throw new UnsupportedFormatException(new Exception(), Locale.US, MessageCodes.TEST_002,
                    TestConstants.DETAIL_ONE, TestConstants.DETAIL_TWO);
        } catch (final UnsupportedFormatException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

    /**
     * Tests the exception thrown when <code>ImageFormat</code> is initialized with bad data.
     */
    @Test
    public void testUnsupportedFormatException() {
        try {
            new ImageFormat("doc");
        } catch (final UnsupportedFormatException details) {
            assertEquals(details.getMessage(),
                    "Supplied format 'doc' is not one of the supported formats: jpg tif tiff pdf gif png webp jp2");
        }
    }

}
