
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.MESSAGES;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.ext.web.RoutingContext;

/**
 * A generic page request handler.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class PageHandler extends JiiifyHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageHandler.class, MESSAGES);

    /**
     * Creates a generic page handler for Jiiify.
     *
     * @param aConfig A application configuration
     */
    public PageHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode jsonNode = mapper.createObjectNode();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.DBG_058, aContext.request().uri());

            aContext.request().params().forEach(param -> {
                LOGGER.debug(MessageCodes.DBG_059, param.getKey(), param.getValue());
            });

            aContext.request().headers().forEach(header -> {
                LOGGER.debug(MessageCodes.DBG_060, header.getKey(), header.getValue());
            });
        }

        // This inserts a session check to toggle our login/logout option
        aContext.data().put(HBS_DATA_KEY, toHbsContext(jsonNode, aContext));
        aContext.next();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
