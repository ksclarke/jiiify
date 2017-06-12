
package info.freelibrary.jiiify.util;

import info.freelibrary.jiiify.Constants;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import ch.qos.logback.classic.Level;

public class LoggingUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtils.class, Constants.MESSAGES);

    private LoggingUtils() {
    }

    /**
     * We're opinionated about loggers, and are using Logback, so can set logging levels at runtime. We could get fancy
     * and make it not dependent on Logback (handle LOG4J and JUL too).
     *
     * @param aLogger A logger whose level we want to change
     * @param aLevel A string version of the desired level
     */
    public static final void setLogLevel(final Logger aLogger, final String aLevel) {
        final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) aLogger.getLoggerImpl();
        final Level currentLevel = logger.getLevel();
        final Level newLevel = Level.toLevel(aLevel, currentLevel);

        LOGGER.debug(MessageCodes.DBG_099, newLevel.levelStr);
        logger.setLevel(newLevel);
    }

    /**
     * We're opinionated about loggers, and are using Logback, so can set logging levels at runtime. We could get fancy
     * and make it not dependent on Logback (handle LOG4J and JUL too).
     *
     * @param aClassToLog A logger whose level we want to change
     * @param aLevel A string version of the desired level
     */
    public static final void setLogLevel(final Class<?> aClassToLog, final String aLevel) {
        setLogLevel(LoggerFactory.getLogger(aClassToLog), aLevel);
    }

    /**
     * Get the logging level of the supplied logger.
     *
     * @param aLogger A logger to check for log level
     * @return The logging level of the supplied logger
     */
    public static final String getLogLevel(final Logger aLogger) {
        return ((ch.qos.logback.classic.Logger) aLogger.getLoggerImpl()).getEffectiveLevel().levelStr;
    }

    /**
     * Get the logging level of the supplied logger class.
     *
     * @param aClassToLog The class of a logger to check for log level
     * @return The logging level of the supplied logger
     */
    public static final String getLogLevel(final Class<?> aClassToLog) {
        return getLogLevel(LoggerFactory.getLogger(aClassToLog));
    }

}
