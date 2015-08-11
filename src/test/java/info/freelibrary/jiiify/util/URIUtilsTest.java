
package info.freelibrary.jiiify.util;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;

import org.junit.Test;

/**
 * Tests for <code>PathUtils</code>.
 *
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class URIUtilsTest {

    /**
     * Tests <code>encodeIdentifier()</code>.
     */
    @Test
    public void testEncodeIdentifier() throws URISyntaxException {
        assertEquals("%2F%3F%23%5B%5D%40%25%2Falpha%2F", PathUtils.encodeIdentifier("/?#[]@%/alpha/"));
    }

    /**
     * Tests <code>encodeServicePrefix()</code>.
     */
    @Test
    public void testEncodeServicePrefix() throws URISyntaxException {
        assertEquals("/%3F%23%5B%5D%40%25/alpha/", PathUtils.encodeServicePrefix("/?#[]@%/alpha/"));
    }

    /**
     * Tests the encoding of non-ASCII characters.
     */
    @Test
    public void testEncodeNonASCIICharacters() throws URISyntaxException {
        assertEquals("/%C3%A9/", PathUtils.encodeServicePrefix("/é/"));
    }

    /**
     * Tests <code>decode()</code>.
     */
    @Test
    public void testDecode() {
        // Handle strings with the percent sign encoded
        assertEquals("/?#[]@%/alpha/", PathUtils.decode("/%3F%23%5B%5D%40%25/alpha/"));

        // But also handle strings that have a regular old percent sign in them
        assertEquals("/?#[]@%/alpha/", PathUtils.decode("/%3F%23%5B%5D%40%/alpha/"));
    }

    /**
     * Tests decode with an empty charset.
     *
     * @throws RuntimeException If the charset name is empty
     */
    @Test(expected = RuntimeException.class)
    public void testDecodeWithBadCharset() throws RuntimeException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        final Method decodeMethod = PathUtils.class.getDeclaredMethod("decode", String.class, String.class);
        decodeMethod.setAccessible(true);

        try {
            decodeMethod.invoke(null, "/?#[]@%/alpha/", "");
        } catch (final InvocationTargetException details) {
            if (details.getCause().getMessage().startsWith("An unsupported charset was supplied")) {
                throw new RuntimeException("Expected exception");
            } else {
                throw details;
            }
        }
    }

    /**
     * Test with different logging levels.
     */
    @Test
    public void testWithLogging() {
        LoggingUtils.setLogLevel(PathUtils.class, "ALL");
        PathUtils.decode(" logging test: ALL ");
        LoggingUtils.setLogLevel(PathUtils.class, "OFF");
        PathUtils.decode(" logging test: OFF ");
    }

    /**
     * The
     *
     * @throws URISyntaxException
     */
    @Test(expected = URISyntaxException.class)
    public void testURISyntaxException() throws URISyntaxException {
        PathUtils.encodeIdentifier("^");
    }

    /**
     * Tests the private constructor.
     *
     * @throws InstantiationException If there is a problem instantiating the class
     * @throws IllegalAccessException If there is a problem accessing the class
     * @throws IllegalArgumentException If there is an illegal argument thrown
     * @throws InvocationTargetException If there is a problem invoking the constructor
     * @throws NoSuchMethodException If the constructor can't be found
     * @throws SecurityException If there is a privilege issue
     */
    @Test
    public void testConstructor() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {
        final Constructor<PathUtils> constructor = PathUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

}