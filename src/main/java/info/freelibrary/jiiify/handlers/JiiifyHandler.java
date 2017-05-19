
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.MESSAGES;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.JsonNodeValueResolver;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * An abstract handler from which other Jiiify handlers can inherit some basic functions.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
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
     * Returns the number of slashes in the supplied ID.
     *
     * @param aID An identifier
     * @return The number of slashes in the identifier
     */
    int slashCount(final String aID) {
        return aID.length() - aID.replace("/", "").length();
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
