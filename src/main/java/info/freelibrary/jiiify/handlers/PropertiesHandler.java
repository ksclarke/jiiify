
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.HBS_DATA_KEY;
import static info.freelibrary.jiiify.Constants.HBS_PATH_SKIP_KEY;
import static info.freelibrary.jiiify.Constants.HTTP_HOST_PROP;
import static info.freelibrary.jiiify.Constants.ID_KEY;
import static info.freelibrary.jiiify.Constants.SERVICE_PREFIX_PROP;
import static info.freelibrary.jiiify.handlers.FailureHandler.ERROR_MESSAGE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.Metadata;
import info.freelibrary.jiiify.util.PathUtils;

import io.vertx.core.file.FileSystem;
import io.vertx.ext.web.RoutingContext;

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
        final FileSystem fileSystem = aContext.vertx().fileSystem();
        final String properties = PathUtils.getFilePath(aContext.vertx(), id, Metadata.PROPERTIES_FILE);

        fileSystem.exists(properties, existsHandler -> {
            if (existsHandler.succeeded()) {
                if (existsHandler.result()) {
                    fileSystem.readFile(properties, readHandler -> {
                        if (readHandler.succeeded()) {
                            processProperties(aContext, readHandler.result().getBytes(), id);
                        } else {
                            fail(aContext, readHandler.cause());
                        }
                    });
                } else {
                    aContext.fail(404);
                    aContext.put(ERROR_MESSAGE, msg("Image properties file ({}) not found", properties));
                }
            } else {
                fail(aContext, existsHandler.cause());
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
