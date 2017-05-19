
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.HBS_PATH_SKIP_KEY;
import static info.freelibrary.jiiify.Constants.HTTP_HOST_PROP;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.SERVICE_PREFIX_PROP;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.util.PathUtils;

import io.vertx.ext.web.RoutingContext;

/**
 * Handler that handles requests for items from the administrative interface.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ItemHandler extends JiiifyHandler {

    /**
     * An item handler for the administrative interface.
     *
     * @param aConfig The application's configuration
     */
    public ItemHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode jsonNode = mapper.createObjectNode();
        final String servicePrefix = myConfig.getServicePrefix();
        final String requestPath = aContext.request().uri();
        final String id;

        if (requestPath.contains("viewer")) {
            id = requestPath.split("\\/")[4];
        } else {
            id = requestPath.split("\\/")[3];
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Getting item page for : {}", id);
        }

        jsonNode.put(ID_KEY, id);
        jsonNode.put(SERVICE_PREFIX_PROP.replace('.', '-'), servicePrefix);
        jsonNode.put(HTTP_HOST_PROP.replace('.', '-'), myConfig.getServer());

        /* To drop the ID from the path for template processing */
        aContext.data().put(HBS_PATH_SKIP_KEY, 1 + slashCount(PathUtils.decode(id)));
        aContext.data().put(HBS_DATA_KEY, toHbsContext(jsonNode, aContext));
        aContext.next();
    }

}
