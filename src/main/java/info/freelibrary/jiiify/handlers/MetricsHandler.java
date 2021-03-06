
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Constants.MESSAGES;
import static info.freelibrary.jiiify.Metadata.CONTENT_TYPE;
import static info.freelibrary.jiiify.Metadata.JSON_MIME_TYPE;

import info.freelibrary.jiiify.Configuration;
import info.freelibrary.jiiify.MessageCodes;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.web.RoutingContext;

/**
 * A handler that handles requests for metrics.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class MetricsHandler extends JiiifyHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsHandler.class, MESSAGES);

    /**
     * Creates a metrics handler.
     *
     * @param aConfig The application's configuration
     */
    public MetricsHandler(final Configuration aConfig) {
        super(aConfig);
    }

    @Override
    public void handle(final RoutingContext aContext) {
        LOGGER.debug(MessageCodes.DBG_057, aContext.request().uri());

        final MetricsService metricsService = MetricsService.create(aContext.vertx());
        final JsonObject metrics = metricsService.getMetricsSnapshot(aContext.vertx());
        final HttpServerResponse response = aContext.response();

        response.headers().add(CONTENT_TYPE, JSON_MIME_TYPE);
        response.end(metrics.toString());
        response.close();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
