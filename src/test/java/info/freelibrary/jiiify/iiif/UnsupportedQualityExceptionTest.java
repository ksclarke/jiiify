
package info.freelibrary.jiiify.iiif;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.TestConstants;

/**
 * An exception thrown when the supplied quality is not a valid IIIF quality.
 *
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class UnsupportedQualityExceptionTest {

    private Locale myDefaultLocale;

    /**
     * Sets up the tests of <code>UnsupportedQualityException</code>.
     *
     * @throws Exception If the test set up fails
     */
    @Before
    public void setUp() throws Exception {
        myDefaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    /**
     * Tears down the tests of <code>UnsupportedQualityException</code>.
     *
     * @throws Exception If the test tear down fails
     */
    @After
    public void tearDown() throws Exception {
        Locale.setDefault(myDefaultLocale);
    }

    /**
     * Tests the exception thrown when <code>ImageQuality</code> is initiated with bad data.
     *
     * @throws UnsupportedQualityException
     */
    @Test(expected = UnsupportedQualityException.class)
    public void testUnsupportedQualityException() throws UnsupportedQualityException {
        throw new UnsupportedQualityException();
    }

    @Test
    public void testUnsupportedQualityExceptionString() {
        try {
            throw new UnsupportedQualityException(MessageCodes.TEST_001);
        } catch (final UnsupportedQualityException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    /**
     * Tests the exception thrown when <code>ImageQuality</code> is initialized with bad data.
     */
    @Test
    public void testUnsupportedQualityExceptionStringObjectArray() {
        try {
            new ImageQuality("bad");
        } catch (final UnsupportedQualityException details) {
            assertEquals("Supplied quality 'bad' is not one of the supported qualities: default color gray bitonal",
                    details.getMessage());
        }
    }

    @Test
    public void testUnsupportedQualityExceptionLocaleString() {
        try {
            throw new UnsupportedQualityException(Locale.US, MessageCodes.TEST_001);
        } catch (final UnsupportedQualityException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testLocaleStringVarargs() {
        try {
            throw new UnsupportedQualityException(Locale.US, MessageCodes.TEST_002, TestConstants.DETAIL_ONE,
                    TestConstants.DETAIL_TWO);
        } catch (final UnsupportedQualityException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

    @Test(expected = UnsupportedQualityException.class)
    public void testThrowable() throws UnsupportedQualityException {
        throw new UnsupportedQualityException(new Exception());
    }

    @Test
    public void testThrowableString() {
        try {
            throw new UnsupportedQualityException(new Exception(), MessageCodes.TEST_001);
        } catch (final UnsupportedQualityException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testThrowableLocaleString() {
        try {
            throw new UnsupportedQualityException(new Exception(), Locale.US, MessageCodes.TEST_001);
        } catch (final UnsupportedQualityException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testThrowableStringVargs() {
        try {
            throw new UnsupportedQualityException(new Exception(), MessageCodes.TEST_002, TestConstants.DETAIL_ONE,
                    TestConstants.DETAIL_TWO);
        } catch (final UnsupportedQualityException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

    @Test
    public void testThrowableLocaleStringVargs() {
        try {
            throw new UnsupportedQualityException(new Exception(), Locale.US, MessageCodes.TEST_002,
                    TestConstants.DETAIL_ONE, TestConstants.DETAIL_TWO);
        } catch (final UnsupportedQualityException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

}
