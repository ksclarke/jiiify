
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.MESSAGES;

import java.io.File;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.JsonNodeValueResolver;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.PairtreeUtils;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

abstract class JiiifyHandler implements Handler<RoutingContext> {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass(), MESSAGES);

    protected final Configuration myConfig;

    /**
     * Creates a default handler from which other Jiiify Handlers can be derived.
     *
     * @param aConfig A Jiiify configuration object
     */
    protected JiiifyHandler(final Configuration aConfig) {
        myConfig = aConfig;
    }

    /**
     * Returns the appropriate Pairtree data directory.
     *
     * @param aID An ID to access
     * @param aConfig An application configuration
     * @return The appropriate Pairtree data directory
     */
    PairtreeRoot getPairtreeRoot(final String aID, final Configuration aConfig) {
        if (aConfig.hasIDPrefixMatch(aID)) {
            final String idPrefix = aConfig.getIDPrefix(aID);

            if (aConfig.hasDataDir(idPrefix)) {
                return aConfig.getDataDir(idPrefix);
            } else {
                LOGGER.warn("Checking an ID with a prefix for which there isn't a data directory");
                return aConfig.getDataDir();
            }
        } else {
            return aConfig.getDataDir();
        }
    }

    /**
     * Returns a Pairtree path for a file in a Pairtree structure.
     *
     * @param aPairtreeRoot The root of the Pairtree structure
     * @param aID The ID of the object in the Pairtree structure
     * @param aFileName The name of the file from the object in the Pairtree structure
     * @return The Pairtree path for the supplied file
     */
    String getPairtreePath(final PairtreeRoot aPairtreeRoot, final String aID, final String aFileName) {
        final String objPath = PairtreeUtils.mapToPtPath(aPairtreeRoot.getAbsolutePath(), aID, aID);
        return objPath + File.separator + aFileName;
    }

    /**
     * Prepares the supplied JSON object for use in the Handlebars context.
     *
     * @param aJsonNode A JSON object
     * @param aContext A context with the current session information
     * @return A Handlebars context that can be passed to the template engine
     */
    Context toHbsContext(final ObjectNode aJsonObject, final RoutingContext aContext) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} JSON passed to template page: {}", getClass().getSimpleName(), aJsonObject.toString());
        }

        // Are we logged into the administrative interface?
        if (aContext.user() == null) {
            aJsonObject.put("logged-in", false);
        } else {
            aJsonObject.put("logged-in", true);
        }

        return Context.newBuilder(aJsonObject).resolver(JsonNodeValueResolver.INSTANCE).build();
    }

    /**
     * Shorthand for a commonly used call that formats a property before passing it to the Handlebars template engine.
     *
     * @param aString A property string that needs to be formatted before giving to the Handlebars template engine
     * @return A string formatted for use by the Handlebars template engine
     */
    String fmt(final String aString) {
        return aString.replace('.', '-');
    }

    /**
     * Shorthand for a commonly used call that formats a message string.
     *
     * @param aMessage A message that needs to be formatted with additional information before using
     * @param aDetails Additional information that will fill in the details of the message string
     * @return A string formatted and ready for use in a message
     */
    String msg(final String aMessage, final Object... aDetails) {
        return LOGGER.getMessage(aMessage, aDetails);
    }

    /**
     * A convenience method for failing a particular context.
     *
     * @param aContext The context to mark as a failure
     * @param aThrowable The exception that caused the context to fail
     */
    void fail(final RoutingContext aContext, final Throwable aThrowable) {
        fail(aContext, aThrowable, aThrowable.getMessage());
    }

    /**
     * A convenience method for failing a particular context.
     *
     * @param aContext The context to mark as a failure
     * @param aThrowable The exception that caused the context to fail
     * @param aMessage A more detailed message to supplement the exception message
     */
    void fail(final RoutingContext aContext, final Throwable aThrowable, final String aMessage) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} is failing this RoutingContext", getClass().getName());
        }

        aContext.fail(500);

        if (aThrowable != null) {
            aContext.fail(aThrowable);
        }

        aContext.put(FailureHandler.ERROR_MESSAGE, aMessage);
    }

    /**
     * A convenience method for failing a particular context.
     *
     * @param aContext The context to mark as a failure
     * @param aFailCode The type of HTTP response failure
     * @param aMessage A more detailed message to supplement the exception message
     */
    void fail(final RoutingContext aContext, final int aFailCode, final String aMessage) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} is failing this RoutingContext", getClass().getName());
        }

        aContext.fail(aFailCode);
        aContext.put(FailureHandler.ERROR_MESSAGE, aMessage);
    }
}
