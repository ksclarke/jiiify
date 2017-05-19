
package info.freelibrary.jiiify.util;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import info.freelibrary.jiiify.Constants;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * A test of {@link info.freelibrary.jiiify.util.LoggingUtils}
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class LoggingUtilsTest {

    // We're not logging this test but just grabbing the logger of the class that we want to test
    private final Logger LOGGER = LoggerFactory.getLogger(LoggingUtils.class, Constants.MESSAGES);

    @Test
    public void testSetLogger() {
        LoggingUtils.setLogLevel(LOGGER, Level.OFF.levelStr);
        assertEquals(Level.OFF.levelStr, LoggingUtils.getLogLevel(LOGGER));
        LoggingUtils.setLogLevel(LOGGER, Level.DEBUG.levelStr);
        assertEquals(Level.DEBUG.levelStr, LoggingUtils.getLogLevel(LoggingUtils.class));
    }

    @Test
    public void testGetLogger() {
        LoggingUtils.setLogLevel(LOGGER, Level.ERROR.levelStr);
        assertEquals(Level.ERROR.levelStr, LoggingUtils.getLogLevel(LOGGER));
        LoggingUtils.setLogLevel(LOGGER, Level.DEBUG.levelStr);
        assertEquals(Level.DEBUG.levelStr, LoggingUtils.getLogLevel(LoggingUtils.class));
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
        final Constructor<LoggingUtils> constructor = LoggingUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

}
