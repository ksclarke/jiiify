
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.HBS_PATH_SKIP_KEY;
import static info.freelibrary.jiiify.Constants.HTTP_HOST_PROP;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.SERVICE_PREFIX_PROP;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.Metadata;
import info.freelibrary.jiiify.util.PathUtils;
import info.freelibrary.pairtree.PairtreeObject;

import io.vertx.ext.web.RoutingContext;

/**
 * A handler that handles requests for property files.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class PropertiesHandler extends JiiifyHandler {

    /**
     * Creates a properties file handler.
     *
     * @param aConfig The application's configuration
     */
    public PropertiesHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final String requestPath = aContext.request().uri();
        final String id = PathUtils.decode(requestPath.split("\\/")[4]);
        final PairtreeObject ptObj = myConfig.getDataDir(id).getObject(id);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Checking for properties file: {}", ptObj.getPath(Metadata.PROPERTIES_FILE));
        }

        ptObj.find(Metadata.PROPERTIES_FILE, findHandler -> {
            if (findHandler.succeeded()) {
                if (findHandler.result()) {
                    ptObj.get(Metadata.PROPERTIES_FILE, getHandler -> {
                        if (getHandler.succeeded()) {
                            processProperties(aContext, getHandler.result().getBytes(), id);
                        } else {
                            fail(aContext, getHandler.cause());
                        }
                    });
                } else {
                    aContext.fail(404);
                    aContext.put(ERROR_MESSAGE, msg("Image properties file not found: {}", Paths.get(ptObj.getPath(),
                            Metadata.PROPERTIES_FILE)));
                }
            } else {
                fail(aContext, findHandler.cause());
            }
        });
    }

    private void processProperties(final RoutingContext aContext, final byte[] aByteArray, final String aID) {
        final ByteArrayInputStream stream = new ByteArrayInputStream(aByteArray);
        final ObjectMapper objMapper = new ObjectMapper();
        final ObjectNode jsonNode = objMapper.createObjectNode();
        final String servicePrefix = myConfig.getServicePrefix();
        final Properties properties = new Properties();

        try {
            properties.loadFromXML(stream);

            /* Put all of our image properties into the template's JSON node */
            for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
                jsonNode.put(fmt((String) entry.getKey()), (String) entry.getValue());
            }

            jsonNode.put(ID_KEY, aID);
            jsonNode.put(fmt(SERVICE_PREFIX_PROP), servicePrefix);
            jsonNode.put(fmt(HTTP_HOST_PROP), myConfig.getServer());

            /* To drop the ID from the path for template processing */
            aContext.data().put(HBS_PATH_SKIP_KEY, 1 + slashCount(aID));
            aContext.data().put(HBS_DATA_KEY, toHbsContext(jsonNode, aContext));
            aContext.next();
        } catch (final IOException details) {
            fail(aContext, details);
        }
    }

}
