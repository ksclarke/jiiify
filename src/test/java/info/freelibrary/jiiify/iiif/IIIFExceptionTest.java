
package info.freelibrary.jiiify.iiif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.TestConstants;

public class IIIFExceptionTest {

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

    @Test(expected = IIIFException.class)
    public void testIIIFException() throws IIIFException {
        throw new IIIFException();
    }

    @Test
    public void testIIIFExceptionString() {
        try {
            new IIIFException(TestConstants.BAD_MSG_KEY);
            fail("Failed to notice a bad message key was supplied");
        } catch (final MissingResourceException details) {
            // This exception is expected...
        }

        try {
            new IIIFException(TestConstants.EMPTY_STRING);
            fail("Could not create an exception without a message");
        } catch (final MissingResourceException details) {
            // This exception is expected...
        }
    }

    @Test
    public void testIIIFExceptionStringObjectArray() {
        try {
            throw new IIIFException(MessageCodes.TEST_002, TestConstants.DETAIL_ONE, TestConstants.DETAIL_TWO);
        } catch (final IIIFException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

    @Test
    public void testIIIFExceptionLocaleString() {
        try {
            throw new IIIFException(Locale.US, MessageCodes.TEST_001);
        } catch (final IIIFException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testIIIFExceptionLocaleStringObjectArray() {
        try {
            throw new IIIFException(Locale.US, MessageCodes.TEST_002, TestConstants.DETAIL_ONE,
                    TestConstants.DETAIL_TWO);
        } catch (final IIIFException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

    @Test(expected = IIIFException.class)
    public void testIIIFExceptionThrowable() throws IIIFException {
        throw new IIIFException(new IOException(TestConstants.TEST_MSG));
    }

    @Test
    public void testIIIFExceptionThrowableString() {
        try {
            throw new IIIFException(new IOException(TestConstants.TEST_MSG), MessageCodes.TEST_001);
        } catch (final IIIFException details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testIIIFExceptionThrowableLocaleString() {
        try {
            throw new IIIFException(new IOException(TestConstants.TEST_MSG), Locale.US, MessageCodes.TEST_001);
        } catch (final Exception details) {
            assertEquals(TestConstants.TEST_MSG, details.getMessage());
        }
    }

    @Test
    public void testIIIFExceptionThrowableStringObjectArray() {
        try {
            throw new IIIFException(new IOException(TestConstants.TEST_MSG), MessageCodes.TEST_002,
                    TestConstants.DETAIL_ONE, TestConstants.DETAIL_TWO);
        } catch (final IIIFException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

    @Test
    public void testIIIFExceptionThrowableLocaleStringObjectArray() {
        try {
            throw new IIIFException(new IOException(TestConstants.TEST_MSG), Locale.US, MessageCodes.TEST_002,
                    TestConstants.DETAIL_ONE, TestConstants.DETAIL_TWO);
        } catch (final IIIFException details) {
            assertEquals(TestConstants.TEST_MSG_DETAILED, details.getMessage());
        }
    }

}
