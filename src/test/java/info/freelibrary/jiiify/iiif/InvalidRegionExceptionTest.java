
package info.freelibrary.jiiify.iiif;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.TestConstants;

/**
 * A test of {@link info.freelibrary.jiiify.iiif.InvalidRegionException}
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class InvalidRegionExceptionTest {

    private Locale myDefaultLocale;

    /**
     * Sets up the tests of <code></code>.
     *
     * @throws Exception If the test set up fails
     */
    @Before
    public void setUp() throws Exception {
        myDefaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    /**
     * Tears down the tests of <code></code>.
     *
     * @throws Exception If the test tear down fails
     */
    @After
    public void tearDown() throws Exception {
        Locale.setDefault(myDefaultLocale);
    }

    @Test(expected = InvalidRegionException.class)
    public void testInvalidRegionException() throws InvalidRegionException {
        throw new InvalidRegionException();
    }

    @Test
    public void testInvalidRegionExceptionString() {
        try {
            throw new InvalidRegionException(MessageCodes.TEST_001);
        } catch (final InvalidRegionException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testInvalidRegionExceptionStringObjectArray() {
        try {
            throw new InvalidRegionException(MessageCodes.TEST_002, TestConstants.DETAIL_ONE, TestConstants.DETAIL_TWO);
        } catch (final InvalidRegionException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

    @Test
    public void testInvalidRegionExceptionLocaleString() {
        try {
            throw new InvalidRegionException(Locale.US, MessageCodes.TEST_001);
        } catch (final InvalidRegionException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testInvalidRegionExceptionLocaleStringObjectArray() {
        try {
            throw new InvalidRegionException(Locale.US, MessageCodes.TEST_002, TestConstants.DETAIL_ONE,
                    TestConstants.DETAIL_TWO);
        } catch (final InvalidRegionException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

    @Test(expected = InvalidRegionException.class)
    public void testInvalidRegionExceptionThrowable() throws InvalidRegionException {
        throw new InvalidRegionException(new Exception());
    }

    @Test
    public void testInvalidRegionExceptionThrowableString() {
        try {
            throw new InvalidRegionException(new Exception(), MessageCodes.TEST_001);
        } catch (final InvalidRegionException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testInvalidRegionExceptionThrowableLocaleString() {
        try {
            throw new InvalidRegionException(new Exception(), Locale.US, MessageCodes.TEST_001);
        } catch (final InvalidRegionException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testInvalidRegionExceptionThrowableStringObjectArray() {
        try {
            throw new InvalidRegionException(new Exception(), MessageCodes.TEST_002, TestConstants.DETAIL_ONE,
                    TestConstants.DETAIL_TWO);
        } catch (final InvalidRegionException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

    @Test
    public void testInvalidRegionExceptionThrowableLocaleStringObjectArray() {
        try {
            throw new InvalidRegionException(new Exception(), Locale.US, MessageCodes.TEST_002,
                    TestConstants.DETAIL_ONE, TestConstants.DETAIL_TWO);
        } catch (final InvalidRegionException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

}
