
package info.freelibrary.jiiify.handlers;

import static info.freelibrary.jiiify.Metadata.CONTENT_TYPE;
import static info.freelibrary.jiiify.Metadata.JSON_MIME_TYPE;

import info.freelibrary.jiiify.Configuration;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.web.RoutingContext;

public class MetricsHandler extends JiiifyHandler {

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
        LOGGER.debug("Requested: {}", aContext.request().uri());

        final MetricsService metricsService = MetricsService.create(aContext.vertx());
        final JsonObject metrics = metricsService.getMetricsSnapshot(aContext.vertx());
        final HttpServerResponse response = aContext.response();

        response.headers().add(CONTENT_TYPE, JSON_MIME_TYPE);
        response.end(metrics.toString());
        response.close();
    }

}
