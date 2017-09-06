
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.HBS_PATH_SKIP_KEY;
import static info.freelibrary.jiiify.Constants.MESSAGES;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.ext.web.RoutingContext;

/**
 * A handler that handles configuration refresh requests.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class RefreshHandler extends JiiifyHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshHandler.class, MESSAGES);

    /**
     * Creates a refresh handler
     *
     * @param aConfig The application's configuration
     */
    public RefreshHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final String requestPath = aContext.request().uri();
        final String id = PathUtils.decode(requestPath.split("\\/")[4]);
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode jsonNode = mapper.createObjectNode();

        LOGGER.debug(MessageCodes.DBG_062, aContext.request().uri());

        /* To drop the ID from the path for template processing */
        aContext.data().put(HBS_PATH_SKIP_KEY, 2 + slashCount(id));
        aContext.data().put(HBS_DATA_KEY, toHbsContext(jsonNode, aContext));
        aContext.next();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
