
package info.freelibrary.jiiify.iiif;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.TestConstants;

public class InvalidRotationExceptionTest {

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

    @Test(expected = InvalidRotationException.class)
    public void testInvalidRotationException() throws InvalidRotationException {
        throw new InvalidRotationException();
    }

    @Test
    public void testInvalidRotationExceptionString() {
        try {
            throw new InvalidRotationException(MessageCodes.TEST_001);
        } catch (final InvalidRotationException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testInvalidRotationExceptionStringObjectArray() {
        try {
            throw new InvalidRotationException(MessageCodes.TEST_002, TestConstants.DETAIL_ONE,
                    TestConstants.DETAIL_TWO);
        } catch (final InvalidRotationException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

    @Test
    public void testInvalidRotationExceptionLocaleString() {
        try {
            throw new InvalidRotationException(Locale.US, MessageCodes.TEST_001);
        } catch (final InvalidRotationException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testInvalidRotationExceptionLocaleStringObjectArray() {
        try {
            throw new InvalidRotationException(Locale.US, MessageCodes.TEST_002, TestConstants.DETAIL_ONE,
                    TestConstants.DETAIL_TWO);
        } catch (final InvalidRotationException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

    @Test(expected = InvalidRotationException.class)
    public void testInvalidRotationExceptionThrowable() throws InvalidRotationException {
        throw new InvalidRotationException(new Exception());
    }

    @Test
    public void testInvalidRotationExceptionThrowableString() {
        try {
            throw new InvalidRotationException(new Exception(), MessageCodes.TEST_001);
        } catch (final InvalidRotationException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testInvalidRotationExceptionThrowableLocaleString() {
        try {
            throw new InvalidRotationException(new Exception(), Locale.US, MessageCodes.TEST_001);
        } catch (final InvalidRotationException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testInvalidRotationExceptionThrowableStringObjectArray() {
        try {
            throw new InvalidRotationException(new Exception(), MessageCodes.TEST_002, TestConstants.DETAIL_ONE,
                    TestConstants.DETAIL_TWO);
        } catch (final InvalidRotationException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

    @Test
    public void testInvalidRotationExceptionThrowableLocaleStringObjectArray() {
        try {
            throw new InvalidRotationException(new Exception(), Locale.US, MessageCodes.TEST_002,
                    TestConstants.DETAIL_ONE, TestConstants.DETAIL_TWO);
        } catch (final InvalidRotationException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

}
