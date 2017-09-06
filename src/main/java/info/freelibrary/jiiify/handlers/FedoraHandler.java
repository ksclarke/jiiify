
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.MESSAGES;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.jiiify.verticles.FedoraIngestVerticle;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * A handler for Fedora repository events routed through Camel. The handler sends requests onto
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class FedoraHandler extends JiiifyHandler {

    public static final String URL_PARAM = "url";

    public static final String FILE_PARAM = "file";

    public static final String IP_PARAM = "ip";

    private static final Logger LOGGER = LoggerFactory.getLogger(FedoraHandler.class, MESSAGES);

    /**
     * Creates a Fedora repository event handler using the supplied configuration.
     *
     * @param aConfig A handler configuration
     */
    public FedoraHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        final HttpServerResponse response = aContext.response();
        final HttpServerRequest request = aContext.request();
        final JsonObject json = new JsonObject();

        json.put(URL_PARAM, request.getParam(URL_PARAM));
        json.put(FILE_PARAM, request.getParam(FILE_PARAM));
        json.put(IP_PARAM, request.remoteAddress().host());

        LOGGER.debug(MessageCodes.DBG_115, json.getString(URL_PARAM), json.getString(FILE_PARAM));

        aContext.vertx().eventBus().send(FedoraIngestVerticle.class.getName(), json, result -> {
            if (result.succeeded()) {
                response.end();
                response.close();
            } else {
                fail(aContext, result.cause(), msg(MessageCodes.EXC_080, FedoraIngestVerticle.class));
            }
        });
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
