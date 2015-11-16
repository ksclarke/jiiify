package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.HBS_PATH_SKIP_KEY;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.util.PathUtils;

import io.vertx.ext.web.RoutingContext;

public class RefreshHandler extends JiiifyHandler {

    public RefreshHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final String requestPath = aContext.request().uri();
        final String id = PathUtils.decode(requestPath.split("\\/")[4]);
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode jsonNode = mapper.createObjectNode();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Using RefreshHandler: {}", aContext.request().uri());
        }

        /* To drop the ID from the path for template processing */
        aContext.data().put(HBS_PATH_SKIP_KEY, 2 + slashCount(id));
        aContext.data().put(HBS_DATA_KEY, toHbsContext(jsonNode, aContext));
        aContext.next();
    }

}
