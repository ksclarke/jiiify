
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.HBS_PATH_SKIP_KEY;
import static info.freelibrary.jiiify.Constants.HTTP_HOST_PROP;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.SERVICE_PREFIX_PROP;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;

import io.vertx.ext.web.RoutingContext;

public class ItemHandler extends JiiifyHandler {

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

        jsonNode.put(ID_KEY, id);
        jsonNode.put(SERVICE_PREFIX_PROP.replace('.', '-'), servicePrefix);
        jsonNode.put(HTTP_HOST_PROP.replace('.', '-'), myConfig.getServer());

        /* To drop the ID from the path for template processing */
        aContext.data().put(HBS_PATH_SKIP_KEY, 1);
        aContext.data().put(HBS_DATA_KEY, toHbsContext(jsonNode, aContext));
        aContext.next();
    }

}
